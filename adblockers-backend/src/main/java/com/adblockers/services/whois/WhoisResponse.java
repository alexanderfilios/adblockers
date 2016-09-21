package com.adblockers.services.whois;

import com.adblockers.utils.Utilities;
import org.springframework.data.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 21/09/16.
 */
public class WhoisResponse {

    private List<WhoisResponseLine> scriptResponse;
    private String domain;

    protected WhoisResponse(List<String> scriptResponse, String domain) {
        this.scriptResponse = scriptResponse.stream()
                .map(WhoisResponseLine::new)
                .collect(Collectors.toList());
        this.domain = domain;
    }

    private List<List<WhoisResponseLine>> getSingledOutResponses() {
        return Utilities.subLists(scriptResponse,
                responseLine -> responseLine.isFirstResponseLineForDomain(domain),
                responseLine -> responseLine.isLastResponseLine());
    }

    /**
     * Reads the WHOIS response and creates a mapping of the properties.
     * Keys are lower case for case-insensitive lookups
     * @return The properties
     */
    private static Map<String, String> getPropertyMappingFromOneResponse(List<WhoisResponseLine> whoisResponse) {
        return whoisResponse.stream()
                .map(responseLine -> responseLine.getPropertyKeyValuePair())
                .filter(keyValuePair -> keyValuePair != null)
                .collect(Collectors.toMap(
                        keyValuePair -> keyValuePair.getFirst(),
                        keyValuePair -> keyValuePair.getSecond(),
                        (option1, option2) -> option1));
    }

    protected Map<String, String> extractPropsMap() {
        // Each response is a list of lines
        return getSingledOutResponses().stream()
                // We convert each response to a mapping of the properties it contains
                .map(whoisResponse -> getPropertyMappingFromOneResponse(whoisResponse))
                // We merge the mappings
                .reduce(new HashMap<>(), (map1, map2) -> Utilities.mergeMaps(map1, map2));
    }

    protected boolean hasMultipleResponses() {
        return scriptResponse.stream()
                .anyMatch(line -> line.contains("To single out one record"));
    }

    private class WhoisResponseLine {
        String scriptResponseLine;

        private WhoisResponseLine(String scriptResponseLine) {
            this.scriptResponseLine = scriptResponseLine;
        }

        private boolean isLastResponseLine() {
            if (scriptResponseLine.trim().length() == 0) {
            }
            return scriptResponseLine.trim().length() == 0;
        }
        private boolean isFirstResponseLineForDomain(String domain) {
            if (scriptResponseLine.toLowerCase().trim().startsWith("domain")
                    && scriptResponseLine.toLowerCase().endsWith(domain.toLowerCase())) {
            }
            return scriptResponseLine.toLowerCase().trim().startsWith("domain")
                    && scriptResponseLine.toLowerCase().endsWith(domain.toLowerCase());
        }
        private boolean contains(String content) {
            return scriptResponseLine.contains(content);
        }

        /**
         * Turns the line "Domain name: google.de" to the {@link Pair} ("Domain name", "google.de")
         * @return The {@link Pair} with the key and the value found, or null if no pair can be extracted
         */
        private Pair<String, String> getPropertyKeyValuePair() {
            String[] keyValue = scriptResponseLine.split(":");
            if (keyValue.length != 2) {
                return null;
            }
            return Pair.of(keyValue[0].trim(), keyValue[1].trim());
        }
    }

}
