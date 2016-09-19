package com.adblockers.converters;

import com.adblockers.entities.HttpRequestRecord;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by alexandrosfilios on 19/09/16.
 */
public class StringToDateConverter implements Converter<String, Date> {
    public Date convert(String source) {
        try {
            System.out.println(source);
            System.out.println(HttpRequestRecord.DATE_FORMAT.format(new Date()));
            return HttpRequestRecord.DATE_FORMAT.parse(source);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date format: DD-MM-yyyy");
        }
    }
}
