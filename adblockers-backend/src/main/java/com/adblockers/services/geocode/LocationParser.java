package com.adblockers.services.geocode;

import org.xml.sax.InputSource;

import java.util.Map;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface LocationParser {
    String getOutputFormat();
    Map<String, String> parseParams(InputSource inputSource, Map<String, String> params);
}
