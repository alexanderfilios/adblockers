package com.adblockers.scripts;

import com.adblockers.utils.LegalEntity;
import com.adblockers.utils.Url;
import com.adblockers.utils.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
@Component
public class WhoisCollector {

    public static final Integer WHOIS_PORT = 43;

    private WhoisRequester whoisRequester;

    /**
     * Allows for a possibly more effective way of getting multiple urls altogether
     * As a fallback solution, URLs are going to be searched une by one
     * @param urls
     * @return
     */
    public Map<Url, LegalEntity> findByUrl(List<Url> urls) {
        return urls.stream()
                .collect(Collectors.toMap(
                        url -> url,
                        url -> findByUrl(url)));
    }

    public LegalEntity findByUrl(Url url) {

        try {
            // When only one record matches, the result will come directly with all details
            List<String> whoisResponse = this.whoisRequester.getResponse(url.getDomain());

            // When multiple results are present, we need to add "=" to expand the details
            if (hasMultipleResponses(whoisResponse)) {
                whoisResponse = this.whoisRequester.getResponse("=" + url.getDomain());
            }

            // We isolate the response
            whoisResponse = WhoisCollector.singleOutResponse(whoisResponse, url.getDomain());

            // We will now read the details and find which WHOIS database has the data for this domain
            Map<String, String> propsMap = extractPropsMap(whoisResponse);
            String whoisServer = propsMap.keySet().stream()
                    .filter(key -> key.toLowerCase().contains("whois server"))
                    .map(key -> propsMap.get(key))
                    .findFirst()
                    .orElse(null);

            // If the server is found, repeat the look on the correct database host
            // Otherwise, keep the result we already had
            if (whoisServer != null) {
                whoisResponse = this.whoisRequester.getResponse(whoisServer, url.getDomain());
            }

            return extractLegalEntity(url.getDomain(),
                    WhoisCollector.singleOutResponse(whoisResponse, url.getDomain()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static boolean hasMultipleResponses(List<String> whoisResponse) {
        return whoisResponse.stream()
                .anyMatch(line -> line.contains("To single out one record"));
    }
    protected static boolean isFirstResponseLine(String line, String url) {
        return line.toLowerCase().trim().startsWith("domain")
                && line.toLowerCase().endsWith(url.toLowerCase());
    }
    protected static boolean isLastResponseLine(String line) {
//        return line.contains(">>> Last update of whois database");
        return line.trim().length() == 0;
    }
    protected static List<String> singleOutResponse(List<String> whoisResponse, String domain) {
        return Utilities.subList(
                whoisResponse,
                line -> WhoisCollector.isFirstResponseLine(line, domain),
                line -> WhoisCollector.isLastResponseLine(line));
    }

    /**
     * Reads the WHOIS response and creates a mapping of the properties.
     * Keys are lower case for case-insensitive lookups
     * @param whoisResponse The WHOIS response
     * @return The properties
     */
    private static Map<String, String> extractPropsMap(List<String> whoisResponse) {
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
    private static LegalEntity extractLegalEntity(String domain, List<String> whoisResponse) {
        Map<String, String> whoisPropsMap = extractPropsMap(whoisResponse);
        return LegalEntity.fromPropertiesMap(domain, whoisPropsMap);
    }

    @Autowired
    public void setWhoisRequester(WhoisRequester whoisRequester) {
        this.whoisRequester = whoisRequester;
    }

}
