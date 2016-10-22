package com.adblockers;

import com.adblockers.converters.StringToBrowserProfile;
import com.adblockers.converters.StringToDateConverter;
import com.adblockers.converters.StringToMetricTypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by alexandrosfilios on 19/09/16.
 */
@EnableWebMvc
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Value("${frontend.domain}") private String frontendDomain;
    @Value("${frontend.port}") private Integer frontendPort;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToBrowserProfile());
        registry.addConverter(new StringToDateConverter());
        registry.addConverter(new StringToMetricTypeConverter());
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://" + frontendDomain + ":" + frontendPort);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }
}
