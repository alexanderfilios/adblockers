package com.adblockers.services.shellscript;

import com.google.common.collect.ImmutableList;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@Service
public class ScriptExecutor implements ShellScriptService {
    private static final Logger LOGGER = Logger.getLogger(ShellScriptService.class);

    public List<String> executeCommand(String command) throws IOException {
        LOGGER.info("Running command: " + command);
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        return readInputStream(process.getInputStream());
    }

    public List<String> executeCommand(String command, Integer timeout)
            throws IOException, TimeoutException, InterruptedException {
        LOGGER.info("Running command: " + command);
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        Worker worker = new Worker(process);
        worker.start();
        try {
            worker.join(timeout);
            if (worker.exit != null) {
                return readInputStream(process.getInputStream());
            } else {
                throw new TimeoutException();
            }
        } catch(InterruptedException e) {
            LOGGER.error("Thread interrupted");
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            process.destroy();
        }
    }
    private static List<String> readInputStream(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> response = new LinkedList<>();
        for (String line = bufferedReader.readLine();
             line != null;
             line = bufferedReader.readLine()) {
            response.add(line);
        }
        return response;
    }

    private static class Worker extends Thread {
        private final Process process;
        private Integer exit;
        private Worker(Process process) {
            this.process = process;
        }
        public void run() {
            try {
                exit = process.waitFor();
            } catch (InterruptedException ignore) {
                return;
            }
        }
    }
}
