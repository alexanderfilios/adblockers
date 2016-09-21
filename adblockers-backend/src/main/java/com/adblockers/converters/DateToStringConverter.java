package com.adblockers.converters;

import com.adblockers.entities.HttpRequestRecord;
import org.springframework.core.convert.converter.Converter;

import java.util.Date;

/**
 * Created by alexandrosfilios on 21/09/16.
 */
public class DateToStringConverter implements Converter<Date, String> {
    public String convert(Date date) {
        return date != null ? HttpRequestRecord.DATE_FORMAT.format(date) : null;
    }
}
