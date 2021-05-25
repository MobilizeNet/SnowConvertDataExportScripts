package mobilize.snowconvert.oracle_export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class QueryProducer {
    public static String EXIT_CODE = "@@@exit!";
    private int workers;
    ExportInfo config;
    Logger logger;
    private int connected_workers = 0;
    private int extracted_queries = 0;
    private Object progress_lock = new Object();
    private BlockingQueue<String> pendingQueriesToExtractData = new LinkedBlockingQueue<String>();
    private int oracleMajorVersion;
    private int oracleMinorVersion;

    public QueryProducer(ExportInfo exportConfig) {
        this.config = exportConfig;
        this.workers = this.config.procceses;
        this.logger = Logger.getLogger("QueryProducer");

        FileHandler fh;

        try {
            fh = new FileHandler(Paths.get(exportConfig.workDir.toString(), "queryproducer.log").toString());
            
            SimpleFormatter formatter = new SimpleFormatter();
            
            fh.setFormatter(formatter);
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            System.out.println("Error setting up logging");
        }
    }

    public void produce() {
        String server = "@" + this.config.host + ":" + this.config.port + ":" + this.config.SID;
        
        System.out.println("Connecting to " + server);
        System.out.println("Export queries at:" + this.config.workDir);
        System.out.println("Data Dumps at:" + this.config.dumpDir);

        ExecutorService executorService = Executors.newFixedThreadPool(workers);
        BlockingQueue<SchemaImportInfo> queue = new ArrayBlockingQueue<SchemaImportInfo>(config.Schemas.size());
        queue.addAll(config.Schemas);

        for (int i = 1; i <= workers; i++) {
            final String workerName = "Worker" + i;

            Runnable task = () -> {
                Connection conn = this.connect(workerName, this.config.host, config.port, config.SID, config.user,
                        config.password);
                this.connected_workers++;

                status();

                if (conn != null) {
                    while (!queue.isEmpty()) {
                        try {
                            SchemaImportInfo info = queue.take();

                            status();

                            this.generateDataQueries(info, conn);
                        } catch (InterruptedException iex) {
                            System.out.println("Interrupted");
                            break;
                        }
                    }

                    executorService.shutdown();
                }
            };

            executorService.execute(task);
        }

        QueryConsumer consumers = new QueryConsumer(config, this.pendingQueriesToExtractData);
        consumers.consume();
        executorService.shutdown();
        this.pendingQueriesToExtractData.add(EXIT_CODE);
        consumers.shutdown();
    }

    public void status() {
        System.out.printf("\r\tConnected %d/%d, %d queries generated", this.connected_workers, workers,
                this.extracted_queries);
    }

    private Connection connect(String workerName, String host, String port, String SID, String user, String password) {
        // Oracle SID = orcl , find yours in tnsname.ora
        try {
            String server = "@" + host + ":" + port + ":" + SID;
            
            logger.info(workerName + ": connecting to " + server);
            
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:" + server, user, password);

            if (conn != null) {
                logger.info(workerName + ": Connected to the database!");

                DatabaseMetaData meta = conn.getMetaData();
                oracleMajorVersion = meta.getDatabaseMajorVersion();
                oracleMinorVersion = meta.getDatabaseMinorVersion();

                return conn;
            } else {
                logger.info(workerName + ":Failed to make connection!");
            }
        } catch (SQLException e) {
            logger.severe(String.format(": SQL State: %s\n%s", e.getSQLState(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void generateDataQueries(SchemaImportInfo info, Connection conn) {
        String sql_schema = "SELECT OWNER,TABLE_NAME, COLUMN_NAME, DATA_TYPE, DATA_LENGTH, DATA_PRECISION, DATA_SCALE FROM all_tab_columns WHERE OWNER = UPPER('"
                + info.SchemaName + "') ";

        if (info.SchemaFilter != null) {
            sql_schema += " AND " + info.SchemaFilter;
        }

        sql_schema += " ORDER BY OWNER,TABLE_NAME,COLUMN_ID";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql_schema);

            String lastTable = "";
            String lastOwner = "";
            List<TableInfo> columns = new ArrayList<TableInfo>();
            
            while (rs.next()) {
                TableInfo tableInfo = new TableInfo();
                String owner = rs.getString("OWNER");
                tableInfo.TableName = rs.getString("TABLE_NAME");
                tableInfo.ColumnName = rs.getString("COLUMN_NAME");
                tableInfo.DataType = rs.getString("DATA_TYPE");
                tableInfo.DataLength = rs.getInt("DATA_LENGTH");
                tableInfo.DataPrecision = rs.getInt("DATA_PRECISION");
                tableInfo.DataScale = rs.getInt("DATA_SCALE");
                
                if (!lastTable.equals(tableInfo.TableName)) {
                    if (columns.size() > 0) // Do we have any columns
                    {
                        this.generateDataQuery(info, lastOwner, lastTable, columns);
                        columns.clear();
                    }
                }

                lastTable = tableInfo.TableName;
                lastOwner = owner;
                columns.add(tableInfo);
            }

            if (columns.size() > 0) // Do we have any columns
            {
                this.generateDataQuery(info, lastOwner, lastTable, columns);
                columns.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generateDataQuery(SchemaImportInfo info, String lastOwner, String lastTable, List<TableInfo> columns) {
        StringBuffer query = new StringBuffer("SELECT ");
        int last = columns.size();
        int current = 1;

        for (TableInfo tableInfo : columns) {
            if (!this.checkIfSupportedType(tableInfo)) {
                logger.info("Column " + tableInfo.ColumnName + " is of type " + tableInfo.DataType + " which is still not supported for extraction.");

                continue;
            }

            String colname = tableInfo.ColumnName;
            
            query.append(colname);
            
            if (current < last) {
                if ((oracleMajorVersion == 12 && oracleMinorVersion > 2) || (oracleMajorVersion < 12)) {
                    query.append(" || ");
                } else {
                    query.append(", ");
                }
            }
            
            current += 1;
        }

        query.append(" FROM ");
        query.append(lastTable);

        if (info.TableFilter != null && !info.TableFilter.trim().isEmpty()) {
            query.append(" WHERE ");
            query.append(info.TableFilter);
        }

        query.append(";");

        Path targetFile = Paths.get(this.config.workDir.toAbsolutePath().normalize().toString(),
                getFileName(lastOwner, lastTable));

        try {
            logger.info("Writing to file: " + targetFile.toString());
            pendingQueriesToExtractData.add(targetFile.toString());
            BufferedWriter writer = Files.newBufferedWriter(targetFile, StandardCharsets.UTF_8);
            writer.write(query.toString());
            writer.flush();
            writer.close();

            synchronized (progress_lock) {
                this.extracted_queries++;
            }

            status();
        } catch (IOException iox) {
            System.out.println("Could not write file " + targetFile);
        }
    }

    private boolean checkIfSupportedType(TableInfo tableInfo) {
        boolean isSupported = true;

        switch(tableInfo.DataType.toUpperCase()) {
            case "BLOB":
            case "NCLOB":
            case "CLOB":
            case "RAW":
            case "BFILE":
                isSupported = false;
                break;
            default:
                isSupported = true;
                break;
        }

        return isSupported;
    }

    private String getFileName(String lastOwner, String lastTable) {
        int num = 1;
        String fileSuffix = UUID.randomUUID().toString().replace("-", "");
        String fileName = lastOwner + "." + lastTable + "." + fileSuffix + ".sql";
        File file = new File(this.config.workDir.toString(), fileName);

        while (file.exists()) {
            fileSuffix = UUID.randomUUID().toString().replace("-", "");
            fileName = lastOwner + "." + lastTable + "." + fileSuffix + " (" + (num++) + ").sql";
            file = new File(this.config.workDir.toString(), fileName);
        }

        return fileName.toLowerCase();
    }
}