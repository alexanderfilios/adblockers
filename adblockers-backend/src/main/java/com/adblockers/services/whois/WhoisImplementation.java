package com.adblockers.services.whois;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.Url;
import com.adblockers.utils.Utilities;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
@Service
public class WhoisImplementation implements WhoisService {

    public static final Integer WHOIS_PORT = 43;
    public static final Logger LOGGER = Logger.getLogger(WhoisService.class);

    private WhoisRequester whoisRequester;

    public LegalEntity findLegalEntityByUrl(Url url) {
        try {
            // When only one record matches, the result will come directly with all details
            WhoisResponse whoisResponse = whoisRequester.getWhoisResponse(url.getDomain(), false);

            // When multiple results are present, we need to add "=" to expand the details
            if (whoisResponse.hasMultipleResponses()) {
                whoisResponse = whoisRequester.getWhoisResponse(url.getDomain(), true);
            }
            // We isolate the responses. Each response is a list of lines
            Map<String, String> propsMap = whoisResponse.extractPropsMap();

            // We will now read the details and find which WHOIS database host has the data for this domain
            // Then we will repeat the same WHOIS query but to the responsible WHOIS database host
            String whoisServer = propsMap.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains("whois server"))
                    .map(entry -> entry.getValue())
                    .findFirst()
                    .orElse(null);

            // If the server is found, repeat the look on the correct database host and merge the props
            // Otherwise, keep the result we already had
            if (whoisServer != null) {
                Map<String, String> secondPropsMap = whoisRequester
                        .getWhoisResponse(whoisServer, url.getDomain(), false)
                        .extractPropsMap();
                propsMap = Utilities.mergeMaps(propsMap, secondPropsMap);
            }

            LegalEntity legalEntity = LegalEntity.fromPropertiesMap(url.getDomain(), propsMap);
            LOGGER.info("WHOIS data found for " + url.getDomain() + ": " + legalEntity.toString());
            return legalEntity;
        } catch (IOException e) {
            LOGGER.warn("No WHOIS data found for " + url.getDomain());
        }
        return LegalEntity.empty(url);
    }

    @Autowired
    public void setWhoisRequester(WhoisRequester whoisRequester) {
        this.whoisRequester = whoisRequester;
    }

}
