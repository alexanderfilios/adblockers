package com.adblockers.services.whois;

import com.sun.istack.internal.Nullable;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public interface WhoisRequester {
    List<String> getResponse(@Nullable String databaseHost, @NotNull String domain, Boolean exact) throws IOException;
    default List<String> getResponse(@NotNull String domain, Boolean exact) throws IOException {
        return getResponse(null, domain, exact);
    }

    default WhoisResponse getWhoisResponse(@Nullable String databaseHost, @NotNull String domain, Boolean exact) throws IOException {
        return new WhoisResponse(getResponse(databaseHost, domain, exact), domain);
    }
    default WhoisResponse getWhoisResponse(@NotNull String domain, Boolean exact) throws IOException {
        return getWhoisResponse(null, domain, exact);
    }
}
