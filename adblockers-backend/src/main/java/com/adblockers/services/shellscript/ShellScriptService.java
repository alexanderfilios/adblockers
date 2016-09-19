package com.adblockers.services.shellscript;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface ShellScriptService {
    List<String> executeCommand(@NotNull String command) throws IOException;
}
