package com.adblockers.services.shellscript;

import java.io.IOException;
import java.util.List;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface ShellScriptService {
    List<String> executeCommand(String command) throws IOException;
}
