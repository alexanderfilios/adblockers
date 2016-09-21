package com.adblockers;

import com.adblockers.converters.*;
import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.HttpRequestRecord;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Date;

/**
 * Created by alexandrosfilios on 19/09/16.
 */
@EnableWebMvc
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToBrowserProfile());
        registry.addConverter(new StringToDateConverter());
        registry.addConverter(new StringToMetricTypeConverter());
    }
}
