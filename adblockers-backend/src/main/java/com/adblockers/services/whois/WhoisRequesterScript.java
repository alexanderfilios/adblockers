package com.adblockers.services.whois;

import com.adblockers.services.shellscript.ScriptExecutor;
import com.adblockers.services.shellscript.ShellScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@Component
public class WhoisRequesterScript implements WhoisRequester {
    private ShellScriptService shellScriptService;

    public List<String> getResponse(String domain) throws IOException {
        return getResponse(null, domain);
    }

    public List<String> getResponse(String databaseHost, String domain) throws IOException {
        StringBuilder commandBuilder = new StringBuilder()
                .append("whois");
        if (databaseHost != null) {
            commandBuilder.append(" -h ")
                    .append(databaseHost)
                    .append(" -p ")
                    .append(WhoisImplementation.WHOIS_PORT);
        }
        commandBuilder.append(" ")
                .append(domain);
        return this.shellScriptService.executeCommand(commandBuilder.toString());
    }

    @Autowired
    public void setShellScriptService(ShellScriptService shellScriptService) {
        this.shellScriptService = shellScriptService;
    }

}
