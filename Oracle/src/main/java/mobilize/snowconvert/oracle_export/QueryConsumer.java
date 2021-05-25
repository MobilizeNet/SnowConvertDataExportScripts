package mobilize.snowconvert.oracle_export;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryConsumer {
    private int workers;
    ExportInfo config;
    Logger logger;
    BlockingQueue<String> queries;
    ExecutorService executorService;

    public QueryConsumer(ExportInfo config, BlockingQueue<String> queries) {
        this.config = config;
        this.workers = config.procceses;
        this.queries = queries;
        this.logger = Logger.getLogger("QueryProducer");

        try {
            FileHandler fh;
            fh = new FileHandler(Paths.get(config.dumpDir.toString(), "queryconsumer.log").toString());
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            System.out.println("Error setting up logging");
        }

        executorService = Executors.newFixedThreadPool(workers);
    }

    public void consume() {
        // Init Consumers
        for (int i = 1; i <= workers; i++) {
            Runnable task = () -> {
                String queryFile;
                try {
                    while (!(queryFile = this.queries.take()).equals(QueryProducer.EXIT_CODE)) {
                        String command = this.config.command;
                        command = command.replace("$user", this.config.user);
                        command = command.replace("$password", this.config.password);
                        command = command.replace("$host", this.config.host);
                        command = command.replace("$port", this.config.port);
                        command = command.replace("$sid", this.config.SID);
                        command = command.replace("$filename", queryFile);
                        command = command.replace("$workdir", this.config.workDir.toString());
                        String outfilename = Paths
                                .get(this.config.dumpDir.toString(), Paths.get(queryFile).getFileName().toString().replace(".sql", ".csv"))
                                .toAbsolutePath().normalize().toString();
                        command = command.replace("$outfilename", outfilename);
                        Process process;
                        
                        try {
                            command = addCommandExecutor(command);
                            logger.info("Running command [" + command + "]");
                            process = Runtime.getRuntime().exec(command);
                            int exitCode = process.waitFor();
                            logger.info("Exit code: " + exitCode);
                        } catch (IOException | InterruptedException ie) {
                            logger.info("Error in command. " + ie.getMessage());
                        }
                    }

                    executorService.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } 
            };
            executorService.execute(task);
        }
    }

    public void shutdown() {
        this.executorService.shutdown();
    }

    private String addCommandExecutor(String command) {
        Pattern p = Pattern.compile("^(\\./)?\\w+\\.(\\w+)?.*");   // the pattern to search for
        Matcher m = p.matcher(command);

        if (m.find()) {
            String fileExtension = m.group(2);
            
            switch(fileExtension) {
                case "ps1":
                    command = "powershell.exe & " + command;
                    break;
                case "bat":
                    command = "cmd.exe /c start /B " + command;
                    break;
                default:
                    break;
            }
        }

        return command;
    }
}