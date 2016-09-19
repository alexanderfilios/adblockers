package com.adblockers.services.whois;

import com.sun.istack.internal.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public interface WhoisRequester {
    List<String> getResponse(@Nullable String databaseHost, @NotNull String domain) throws IOException;
    List<String> getResponse(@NotNull String domain) throws IOException;
}
