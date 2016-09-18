package com.adblockers.entities;

import com.adblockers.services.requestgraph.RequestGraph;
import org.springframework.data.annotation.Transient;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public class Metric {

    public enum MetricType {
        FPD_DEGREE,
        FPD_DEGREE_TOP_10,
        FPD_DEGREE_TOP_1,
        TPD_DEGREE,
        TPD_DEGREE_TOP_10,
        TPD_DEGREE_TOP_1,
        DENSITY
    }

    @Transient private Date dateObject;
    private String date;
    private Double value;
    private MetricType metricType;
    private RequestGraph.RequestGraphType requestGraphType;
    private BrowserProfile browserProfile;

    public Metric() {}

    public Metric(Date date, Double value, MetricType metricType, RequestGraph.RequestGraphType requestGraphType, BrowserProfile browserProfile) {
        setDate(date);
        setValue(value);
        setMetricType(metricType);
        setRequestGraphType(requestGraphType);
        setBrowserProfile(browserProfile);
    }

    public void setDate(String date) {
        try {
            this.dateObject = HttpRequestRecord.DATE_FORMAT.parse(date);
            this.date = date;
        } catch (ParseException e) {}
    }
    public void setDate(Date date) {
        this.date = HttpRequestRecord.DATE_FORMAT.format(date);
        this.dateObject = date;
    }
    public Date getDate() {
        return this.dateObject;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    public RequestGraph.RequestGraphType getRequestGraphType() {
        return requestGraphType;
    }

    public void setRequestGraphType(RequestGraph.RequestGraphType requestGraphType) {
        this.requestGraphType = requestGraphType;
    }

    public BrowserProfile getBrowserProfile() {
        return browserProfile;
    }

    public void setBrowserProfile(BrowserProfile browserProfile) {
        this.browserProfile = browserProfile;
    }
}
