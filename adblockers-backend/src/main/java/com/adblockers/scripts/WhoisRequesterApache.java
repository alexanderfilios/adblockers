package com.adblockers.scripts;

import org.apache.commons.net.whois.WhoisClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alexandrosfilios on 17/09/16.
 */

public class WhoisRequesterApache implements WhoisRequester {
    public List<String> getResponse(String domain) throws IOException {
        return getResponse(WhoisClient.DEFAULT_HOST);
    }
    public List<String> getResponse(String databaseHost, String domain) throws IOException {
        WhoisClient whoisClient = new WhoisClient();
        whoisClient.connect(databaseHost);
        String whoisResponse = whoisClient.query(domain);
        whoisClient.disconnect();
        return Arrays.asList(whoisResponse.split(System.lineSeparator()));
    }
}
