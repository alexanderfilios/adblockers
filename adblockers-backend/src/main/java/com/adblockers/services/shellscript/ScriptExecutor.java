package com.adblockers.services.shellscript;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@Component
public class ScriptExecutor implements ShellScriptService {
    private static final Logger LOGGER = Logger.getLogger(ScriptExecutor.class);

    public List<String> executeCommand(String command) throws IOException {
        LOGGER.info("Running command: " + command);
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        InputStream inputStream = process.getInputStream();
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
}
