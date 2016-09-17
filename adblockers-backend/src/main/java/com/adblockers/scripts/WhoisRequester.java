package com.adblockers.scripts;

import java.io.IOException;
import java.util.List;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public interface WhoisRequester {
    List<String> getResponse(String databaseHost, String domain) throws IOException;
    List<String> getResponse(String domain) throws IOException;
}
