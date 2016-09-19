package com.adblockers.converters;

import com.adblockers.entities.BrowserProfile;
import org.springframework.core.convert.converter.Converter;


/**
 * Created by alexandrosfilios on 19/09/16.
 */
public final class StringToBrowserProfile implements Converter<String, BrowserProfile> {
    public BrowserProfile convert(String source) {
        return BrowserProfile.from(source);
    }
}
