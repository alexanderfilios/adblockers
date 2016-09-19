package com.adblockers;

import com.adblockers.converters.StringToBrowserProfile;
import com.adblockers.converters.StringToDateConverter;
import com.adblockers.converters.StringToMetricTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
