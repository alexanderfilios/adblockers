package com.adblockers.entities;

import com.adblockers.services.requestgraph.RequestGraph;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by alexandrosfilios on 20/10/16.
 */
@Document(collection = "statistics")
public class MetricAdapter {

    private String name;
    @Field("value") private String valueString;
    private String instance;
    private String crawlDate;

    @Transient private static Logger LOGGER = Logger.getLogger(MetricAdapter.class);

    @Transient private static Map<String, Metric.MetricType> METRIC_TYPE_MAP = ImmutableMap.<String, Metric.MetricType>builder()
            .put("first-stdev", Metric.MetricType.FPD_DEGREE_STDEV)
            .put("first-means", Metric.MetricType.FPD_DEGREE_MEAN)
            .put("first-mean-top10", Metric.MetricType.FPD_DEGREE_MEAN_TOP_10)
            .put("first-mean-top1", Metric.MetricType.FPD_DEGREE_MEAN_TOP_1)
            .put("top500-first-means", Metric.MetricType.FPD_DEGREE_MEAN_TOP_500)
            .put("last500-first-means", Metric.MetricType.FPD_DEGREE_MEAN_LAST_500)

            .put("third-means", Metric.MetricType.TPD_DEGREE_MEAN)
            .put("third-stdev", Metric.MetricType.TPD_DEGREE_STDEV)
            .put("third-mean-top10", Metric.MetricType.TPD_DEGREE_MEAN_TOP_10)
            .put("third-mean-top1", Metric.MetricType.TPD_DEGREE_MEAN_TOP_1)

            .put("density", Metric.MetricType.DENSITY)

            .put("misclassified", Metric.MetricType.UNDEFINED)
            .put("unrecognized", Metric.MetricType.UNDEFINED)
            .put("nostats", Metric.MetricType.UNDEFINED)
            .build();
    @Transient Pattern ENTITY_PATTERN = Pattern.compile("(-entities)$");
    @Transient public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    private RequestGraph.RequestGraphType getRequestGraphType() {
        return getName().contains("entities")
                ? RequestGraph.RequestGraphType.ENTITY_REQUEST_GRAPH
                : RequestGraph.RequestGraphType.DOMAIN_REQUEST_GRAPH;
    }
    private Metric.MetricType getMetricType() {
        return METRIC_TYPE_MAP.getOrDefault(ENTITY_PATTERN.matcher(getName()).replaceAll(""), null);
    }
    private Double getValue() {
        try {
            return Double.valueOf(getValueString());
        } catch (NumberFormatException e) {
            LOGGER.error("Failed to convert value: " + getValueString());
            return null;
        }
    }
    private Date getDate() {
        try {
            return DATE_FORMAT.parse(getCrawlDate());
        } catch (ParseException e) {
            LOGGER.error("Failed to convert date: " + getCrawlDate());
            return null;
        }
    }
    private BrowserProfile getBrowserProfile() {
        BrowserProfile.Adblocker adblocker = getInstance().contains("Ghostery") ? BrowserProfile.Adblocker.GHOSTERY
                : getInstance().contains("Adblockplus") ? BrowserProfile.Adblocker.ADBLOCKPLUS
                : getInstance().contains("NoAdblocker") ? BrowserProfile.Adblocker.NOADBLOCKER
                : null;
        BrowserProfile.UserAgent userAgent = getInstance().contains("MUA")
                ? BrowserProfile.UserAgent.MOBILE
                : BrowserProfile.UserAgent.DESKTOP;
        BrowserProfile.ProtectionLevel protectionLevel = (BrowserProfile.Adblocker.NOADBLOCKER.equals(adblocker) && getInstance().contains("DNT"))
                || (!BrowserProfile.Adblocker.NOADBLOCKER.equals(adblocker) && getInstance().contains("MaxProtection"))
                ? BrowserProfile.ProtectionLevel.MAX
                : BrowserProfile.ProtectionLevel.DEFAULT;

        return BrowserProfile.from(adblocker, protectionLevel, userAgent);
    }

    public Metric toMetric() {
        return Metric.from(getDate(), getValue(), getMetricType(), getRequestGraphType(), getBrowserProfile());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getCrawlDate() {
        return crawlDate;
    }

    public void setCrawlDate(String crawlDate) {
        this.crawlDate = crawlDate;
    }
}
