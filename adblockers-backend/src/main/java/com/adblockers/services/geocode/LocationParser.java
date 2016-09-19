package com.adblockers.services.geocode;

import org.xml.sax.InputSource;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface LocationParser {
    String getOutputFormat();
    Map<String, String> parseParams(@NotNull InputSource inputSource, @NotNull Map<String, String> params);
}
