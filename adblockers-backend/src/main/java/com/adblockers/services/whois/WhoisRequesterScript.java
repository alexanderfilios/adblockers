package com.adblockers.services.whois;

import com.adblockers.services.shellscript.ScriptExecutor;
import com.adblockers.services.shellscript.ShellScriptService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@Component
public class WhoisRequesterScript implements WhoisRequester {

    private static final Integer TIMEOUT = 3000;
    private static final Logger LOGGER = Logger.getLogger(WhoisRequester.class);

    private ShellScriptService shellScriptService;



    public List<String> getResponse(String databaseHost, String domain, Boolean exact) throws IOException {
        StringBuilder commandBuilder = new StringBuilder()
                .append("whois");
        if (databaseHost != null) {
            commandBuilder.append(" -h ")
                    .append(databaseHost)
                    .append(" -p ")
                    .append(WhoisImplementation.WHOIS_PORT);
        }
        commandBuilder.append(" ")
                .append(exact ? "=" : "")
                .append(domain);
        try {
            return this.shellScriptService.executeCommand(commandBuilder.toString(), TIMEOUT);
        } catch (InterruptedException | TimeoutException e) {
            LOGGER.warn("WHOIS request timed out.");
            throw new IOException();
        }
    }

    @Autowired
    public void setShellScriptService(ShellScriptService shellScriptService) {
        this.shellScriptService = shellScriptService;
    }

}
