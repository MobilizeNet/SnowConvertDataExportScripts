package mobilize.snowconvert.oracle_export;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import java.util.List;

/**
 * Oracle Exporter
 * Connects to an Oracle Database and performs an extraction of the specified tables
 */
public class App {
    public static void info() {
        System.out.println("Oracle Export Tool");
        System.out.println("=======================");
        System.out.println("This is a simple tool to dump tables to CSV files");
        System.out.println("Usage: java -jar <jar file generated from compilation>.jar -host <host> -port <port> {-sid <sid> | -serviceName <serviceName>} -user <user> -password <password> [-configFile <path to exportconfig.yaml if omitted assumes the file is on working dir>]");
    }

    public static void main(String[] args) throws IOException {
        ExportInfo config = new ExportInfo();

        info();
        loadConfig(args, config);
        
        QueryProducer producer = new QueryProducer(config);
        producer.produce();
    }

    private static void loadConfig(String[] args, ExportInfo exportConfig) throws IOException {
        if (args.length % 2 != 0 || args.length < 10) {
            System.out.println("It seems like there is some inconsistency in the arguments");
            System.exit(100);
        }

        LinkedHashMap<String, String> argsProcessed = processArgs(args);
        
        exportConfig.host = argsProcessed.get("host");
        exportConfig.port = argsProcessed.get("port");
        exportConfig.SID = argsProcessed.get("sid") == null ? "" : argsProcessed.get("sid");
        exportConfig.serviceName = argsProcessed.get("servicename") == null ? "" : argsProcessed.get("servicename");
        exportConfig.user = argsProcessed.get("user");
        exportConfig.password = argsProcessed.get("password");
        
        String configFile = argsProcessed.get("configfile");

        final File initialFile = new File(configFile);
        
        if (!initialFile.exists()) {
            exit_no_configFound();
        }

        Yaml yaml = new Yaml();
        final InputStream configStream = new FileInputStream(initialFile);
        Map<String, Object> config = yaml.load(configStream);
        
        checkConfigInfo(exportConfig, config);
        
        LinkedHashMap<String, Object> schemas = (LinkedHashMap<String, Object>) config.get("schemas");
        
        for (Map.Entry<String, Object> schema : schemas.entrySet()) {
            String schemaName = schema.getKey();
            List<Map<String, Object>> settings = (List<Map<String, Object>>) schema.getValue();
            String schemaFilter = null;
            String tableFilter = null;

            if (settings != null) {
                for (Map<String, Object> setting : settings) {
                    schemaFilter = (String) setting.get("schemaFilter");
                    tableFilter = (String) setting.get("queryFilter");

                    SchemaImportInfo schemaConfigItem = new SchemaImportInfo(schemaName, schemaFilter, tableFilter);
                    exportConfig.Schemas.add(schemaConfigItem);
                }
            } else {
                SchemaImportInfo schemaConfigItem = new SchemaImportInfo(schemaName, schemaFilter, tableFilter);
                exportConfig.Schemas.add(schemaConfigItem);
            }
        }

        System.out.println("Config file loaded!");
    }

    private static LinkedHashMap<String, String> processArgs(String[] args) {
        LinkedHashMap<String, String> argsProcessed= new LinkedHashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            switch(args[i].toLowerCase()) {
                case "-host":
                    i++;
                    argsProcessed.put("host", args[i]);

                    break;
                case "-port":
                    i++;
                    argsProcessed.put("port", args[i]);

                    break;
                case "-sid":
                    i++;
                    argsProcessed.put("sid", args[i]);

                    break;
                case "-servicename":
                    i++;
                    argsProcessed.put("servicename", args[i]);
                    
                    break;
                case "-user":
                    i++;
                    argsProcessed.put("user", args[i]);

                    break;
                case "-password":
                    i++;
                    argsProcessed.put("password", args[i]);

                    break;
                case "-configfile":
                    i++;
                    argsProcessed.put("configfile", args[i].trim().isEmpty() ? "exportconfig.yaml" : args[i]);

                    break;
            }
        }

        return argsProcessed;
    }

    private static void checkConfigInfo(ExportInfo exportConfig, Map<String, Object> config) throws IOException {
        int processesRunning = (int) config.get("processes");
        String description = (String) config.get("description");
        String workDir = (String) config.get("exportDir");
        String dumpDir = (String) config.get("dumpDir");
        String command = (String) config.get("extractor");

        // Validate command
        if (command == null) {
            System.out.println("No export command was provided. Please setup one in your config file");
            System.exit(100);
        }

        // Check directories settings
        if (workDir == null) {
            workDir = "./workdir";
        }

        if (dumpDir == null) {
            dumpDir = "./dumpdir";
        }
        
        // Validate if workdir is valid and create if needed
        File workDirFile = new File(workDir);
        if (workDirFile.exists()) {
            if (!workDirFile.isDirectory()) {
                System.out.println(workDir + " is not a directory");
                System.exit(100);
            }
        } else {
            Files.createDirectories(workDirFile.toPath());
        }

        // Validate if dumpdir is valid and create if needed
        File dumpDirFile = new File(dumpDir);
        if (dumpDirFile.exists()) {
            if (!dumpDirFile.isDirectory()) {
                System.out.println(dumpDir + " is not a directory");
                System.exit(100);
            }
        } else {
            Files.createDirectories(dumpDirFile.toPath());
        }

        exportConfig.workDir = workDirFile.toPath();
        exportConfig.dumpDir = dumpDirFile.toPath();
        exportConfig.description = description;
        exportConfig.command = command;
        exportConfig.procceses = processesRunning;
    }

    private static void exit_no_configFound() {
        System.out.println("Config file could not be loaded. Abort");
        System.exit(2);
    }
}
