package mobilize.snowconvert.oracle_export;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryConsumer {
    private int workers;
    ExportInfo config;
    Logger queryConsumerLogger;
    BlockingQueue<String> queries;
    ExecutorService executorService;
    private static volatile int extracted_datadumps = 0;
    private static final Object query_consumer_progress_lock = new Object();

    public QueryConsumer(ExportInfo config, BlockingQueue<String> queries) {
        this.config = config;
        this.workers = config.procceses;
        this.queries = queries;
        this.queryConsumerLogger = Logger.getLogger(this.getClass().getSimpleName());

        try {
            FileHandler fh;
            fh = new FileHandler(Paths.get(config.dumpDir.toString(), "queryconsumer.log").toString());

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            queryConsumerLogger.addHandler(fh);
            queryConsumerLogger.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            System.out.println("Error setting up logging");
        }

        executorService = Executors.newCachedThreadPool();
    }

    public void consume() {
        // Init Consumers
        for(int i = 0; i < workers*2; i++) {
            Runnable task = () -> {
                String queryFile;
                try {
                    while (!(queryFile = this.queries.take()).equals(QueryProducer.EXIT_CODE)) {
                        String command = this.config.command;
                        command = addCommandExecutor(command);
                        command = command.replace("$user", this.config.user);
                        command = command.replace("$password", this.config.password);
                        command = command.replace("$host", this.config.host);
                        command = command.replace("$port", this.config.port);
                        command = command.replace("$sid", this.config.SID.trim().isEmpty() ? "null" : this.config.SID);
                        command = command.replace("$servicename", this.config.serviceName.trim().isEmpty() ? "null" : this.config.serviceName);
                        command = command.replace("$filename", queryFile);
                        command = command.replace("$workdir", this.config.workDir.toString());
                        String outfilename = Paths
                                .get(this.config.dumpDir.toString(), Paths.get(queryFile).getFileName().toString().replace(".sql", ".csv"))
                                .toAbsolutePath().normalize().toString();
                        command = command.replace("$outfilename", outfilename);

                        try {
                            queryConsumerLogger.info("Running command [" + command + "]");

                            String[] commandAndParameters = command.split(" ");

                            ProcessBuilder builder = new ProcessBuilder();
                            builder.redirectErrorStream(true);
                            builder.command(commandAndParameters);

                            Instant beginning = Instant.now();

                            Process proc = builder.start();
                            int exitCode = proc.waitFor();

                            Instant finishing = Instant.now();

                            synchronized (query_consumer_progress_lock) {
                                QueryConsumer.extracted_datadumps++;
                            }

                            status();
                            
                            if (exitCode == 0) {
                                queryConsumerLogger.info("Exit code: " + exitCode + ". It took " + Duration.between(beginning, finishing).getSeconds() + " seconds to execute the command [" + command + "]");
                            } else {
                                queryConsumerLogger.warning("Exit code: " + exitCode + ". Something happened with the execution of the command [" + command + "]. It took " + Duration.between(beginning, finishing).getSeconds() + " seconds to execute it");
                            }
                        } catch (IOException ie) {
                            queryConsumerLogger.severe("Error: " + ie.getMessage() + " in command " + command);
                        }
                    }

                    this.queries.add(QueryProducer.EXIT_CODE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

            executorService.execute(task);
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(75, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String addCommandExecutor(String command) {
        Pattern p = Pattern.compile("^(\\./)?\\w+\\.(\\w+)?.*");
        Matcher m = p.matcher(command);

        if (m.find()) {
            String fileExtension = m.group(2);

            switch (fileExtension) {
                case "ps1":
                    command = "powershell.exe " + command;
                    break;
                case "bat":
                    command = "cmd.exe /c start /B " + command;
                    break;
                case "sh":
                    command = "bash " + command;
                    break;
                default:
                    break;
            }
        }

        return command;
    }

    private void status() {
        // Send it to the log file to avoid crashing the output message with the console message from the QueryProducer
        queryConsumerLogger.info("Exported " + QueryConsumer.extracted_datadumps + " datadumps");
    }
}