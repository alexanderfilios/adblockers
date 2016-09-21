package com.adblockers.services.whois;

import org.apache.commons.net.whois.WhoisClient;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alexandrosfilios on 17/09/16.
 */

public class WhoisRequesterApache implements WhoisRequester {

    public List<String> getResponse(String databaseHost, String domain, Boolean exact) throws IOException {
        if (StringUtils.isEmpty(databaseHost)) {
            databaseHost = WhoisClient.DEFAULT_HOST;
        }
        WhoisClient whoisClient = new WhoisClient();
        whoisClient.connect(databaseHost);
        String whoisResponse = whoisClient.query((exact ? "=" : "") + domain);
        whoisClient.disconnect();
        return Arrays.asList(whoisResponse.split(System.lineSeparator()));
    }
}
