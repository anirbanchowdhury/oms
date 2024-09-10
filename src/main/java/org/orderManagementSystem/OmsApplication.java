package org.orderManagementSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class OmsApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(OmsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(OmsApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        executeShellScript("","/Users/aniamritapc/Downloads/kafka_2.13-3.8.0/bin/zookeeper-server-start.sh /Users/aniamritapc/Downloads/kafka_2.13-3.8.0/config/zookeeper.properties");
        executeShellScript("","/Users/aniamritapc//Downloads/kafka_2.13-3.8.0/bin/kafka-server-start.sh /Users/aniamritapc/Downloads/kafka_2.13-3.8.0/config/server.properties");
    }


    public void executeShellScript(String processName,String pathToProcessName) throws Exception {
        try {
            // Check if the process is already running (using 'pgrep' for example)
            Process checkProcess = Runtime.getRuntime().exec(new String[] { "pgrep", "-f", processName });

            // Read the output to see if the process is already running
            BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()));
            String line = reader.readLine();

            // If there is output from `pgrep`, the process is already running
            if (line != null && !line.isEmpty()) {
                logger.error("Process {}  is already running. No need to start the script." , processName  );
                return;
            }

            // If the process is not found, execute the shell script
            //;
            Process process = Runtime.getRuntime().exec(pathToProcessName);

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Script {} executed successfully!",pathToProcessName);
            } else {
                logger.error("Script {} failed with exit code {}" ,pathToProcessName, exitCode);
            }

        } catch (IOException | InterruptedException e) {
            throw e; //for now, i want to know all exceptions
        }
    }
}

