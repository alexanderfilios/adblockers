package com.adblockers.controllers;

import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.Metric;
import com.adblockers.services.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("metrics/")
public class MetricController {

    private MetricService metricService;

    @RequestMapping(value = {"entitygraph/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeEntityGraphMetricsForDate(@PathVariable Date crawlDate) {
        this.metricService.storeEntityGraphMetricsForDate(crawlDate);
    }

    @RequestMapping(value = {"entitygraph/{browserProfile}/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeEntityGraphMetricsForProfileAndDate(
            @PathVariable BrowserProfile browserProfile,
            @PathVariable Date crawlDate
    ) {
        this.metricService.storeEntityGraphMetricsForProfileAndDate(browserProfile, crawlDate);
    }

    @RequestMapping(value = {"domaingraph/{metricType}"}, method = RequestMethod.GET)
    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getDomainGraphMetricForBrowserProfile(
            @PathVariable Metric.MetricType metricType
    ) {
        return this.metricService.getDomainGraphMetricForBrowserProfile(metricType);
    }
    @RequestMapping(value = {"domaingraph"}, method = RequestMethod.GET)
    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getDomainGraphMetrics() {
        return this.metricService.getDomainGraphMetrics();
    }

    @RequestMapping(value = {"entitygraph/{metricType}"}, method = RequestMethod.GET)
    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getEntityGraphMetricForBrowserProfile(
            @PathVariable Metric.MetricType metricType
    ) {
        return this.metricService.getDomainGraphMetricForBrowserProfile(metricType);
    }
    @RequestMapping(value = {"entitygraph"}, method = RequestMethod.GET)
    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getEntityGraphMetrics() {
        return this.metricService.getEntityGraphMetrics();
    }

    @RequestMapping(value = {"domaingraph/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeDomainGraphMetricsForDate(@PathVariable Date crawlDate) {
        this.metricService.storeDomainGraphMetricsForDate(crawlDate);
    }

    @RequestMapping(value = {"domaingraph/{browserProfile}/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeDomainGraphMetricsForProfileAndDate(
            @PathVariable BrowserProfile browserProfile,
            @PathVariable Date crawlDate
    ) {
        this.metricService.storeDomainGraphMetricsForProfileAndDate(browserProfile, crawlDate);
    }

    @Autowired
    public void setMetricService(MetricService metricService) { this.metricService = metricService; }
}
