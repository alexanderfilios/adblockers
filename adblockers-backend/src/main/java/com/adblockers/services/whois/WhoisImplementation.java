package com.adblockers.services.whois;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.Url;
import com.adblockers.utils.Utilities;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
@Component
public class WhoisImplementation implements WhoisService {

    public static final Integer WHOIS_PORT = 43;
    public static final Logger LOGGER = Logger.getLogger(WhoisService.class);

    private WhoisRequester whoisRequester;

    public LegalEntity findLegalEntityByUrl(Url url) {

        try {
            // When only one record matches, the result will come directly with all details
            List<String> whoisResponse = this.whoisRequester.getResponse(url.getDomain());

            // When multiple results are present, we need to add "=" to expand the details
            if (hasMultipleResponses(whoisResponse)) {
                whoisResponse = this.whoisRequester.getResponse("=" + url.getDomain());
            }
            // We isolate the responses. Each response is a list of lines
            Map<String, String> propsMap = extractPropsMapFromResponses(
                    singleOutResponses(whoisResponse, url.getDomain()));

            // We will now read the details and find which WHOIS database has the data for this domain
            String whoisServer = propsMap.entrySet().stream()
                    .filter(entry -> entry.getKey().toLowerCase().contains("whois server"))
                    .map(entry -> entry.getValue())
                    .findFirst()
                    .orElse(null);

            // If the server is found, repeat the look on the correct database host and merge the props
            // Otherwise, keep the result we already had
            if (whoisServer != null) {
                whoisResponse = this.whoisRequester.getResponse(whoisServer, url.getDomain());
                Map<String, String> secondPropsMap = extractPropsMapFromResponses(
                        singleOutResponses(whoisResponse, url.getDomain()));
                propsMap = Utilities.mergeMaps(propsMap, secondPropsMap);
            }

            LegalEntity legalEntity = LegalEntity.fromPropertiesMap(url.getDomain(), propsMap);
            LOGGER.info("WHOIS data found for " + url.getDomain() + ": " + legalEntity.toString());
            return legalEntity;
        } catch (IOException e) {
            LOGGER.warn("No WHOIS data found for " + url.getDomain());
        }
        return null;
    }

    private static boolean hasMultipleResponses(List<String> whoisResponse) {
        return whoisResponse.stream()
                .anyMatch(line -> line.contains("To single out one record"));
    }
    private static boolean isFirstResponseLine(String line, String url) {
        return line.toLowerCase().trim().startsWith("domain")
                && line.toLowerCase().endsWith(url.toLowerCase());
    }
    private static boolean isLastResponseLine(String line) {
        return line.trim().length() == 0;
    }

    private static List<List<String>> singleOutResponses(List<String> whoisResponse, String domain) {
        return Utilities.subLists(whoisResponse,
                line -> WhoisImplementation.isFirstResponseLine(line, domain),
                line -> WhoisImplementation.isLastResponseLine(line));
    }

    /**
     * Reads the WHOIS response and creates a mapping of the properties.
     * Keys are lower case for case-insensitive lookups
     * @param whoisResponse The WHOIS response
     * @return The properties
     */
    private static Map<String, String> extractPropsMapFromResponse(List<String> whoisResponse) {
        return whoisResponse.stream()
                // "Domain: google.de"
                .map(line -> line.split(":"))
                // {"Domain", " google.de"}
                .filter(lineArray -> lineArray.length == 2)
                // {"Domain": "google.de"}
                .collect(Collectors.toMap(
                        lineArray -> lineArray[0].trim(),
                        lineArray -> lineArray[1].trim(),
                        (option1, option2) -> option1));
    }
    private static Map<String, String> extractPropsMapFromResponses(List<List<String>> whoisResponses) {
        // Each response is a list of lines
        return whoisResponses.stream()
                // We convert each response to a mapping of the properties it contains
                .map(whoisResponse -> extractPropsMapFromResponse(whoisResponse))
                // We merge the mappings
                .reduce(new HashMap<>(), (map1, map2) -> Utilities.mergeMaps(map1, map2));
    }

    @Autowired
    public void setWhoisRequester(WhoisRequester whoisRequester) {
        this.whoisRequester = whoisRequester;
    }

}
