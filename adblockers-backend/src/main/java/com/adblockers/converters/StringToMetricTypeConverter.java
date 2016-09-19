package com.adblockers.converters;

import com.adblockers.entities.Metric;
import org.springframework.core.convert.converter.Converter;

/**
 * Created by alexandrosfilios on 19/09/16.
 */
public class StringToMetricTypeConverter implements Converter<String, Metric.MetricType> {
    public Metric.MetricType convert(String source) {
        System.out.println(source + " should be converted");

        return Metric.MetricType.valueOf(source);
    }
}
