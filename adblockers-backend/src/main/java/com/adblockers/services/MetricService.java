package com.adblockers.services;

import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.HttpRequestRecord;
import com.adblockers.entities.Metric;
import com.adblockers.repos.HttpRequestRecordRepository;
import com.adblockers.repos.MetricRepository;
import com.adblockers.services.requestgraph.RequestGraph;
import com.adblockers.services.requestgraph.RequestGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 22/10/16.
 */
@Service
public class MetricService {
    private HttpRequestRecordRepository httpRequestRecordRepository;
    private MetricRepository metricRepository;
    private RequestGraphService requestGraphService;

    public void storeEntityGraphMetricsForDate(Date crawlDate) {
        BrowserProfile.getAllBrowserProfiles()
                .forEach(browserProfile -> storeEntityGraphMetricsForProfileAndDate(browserProfile, crawlDate));
    }

    public void storeEntityGraphMetricsForProfileAndDate(
            BrowserProfile browserProfile,
            Date crawlDate
    ) {
        List<HttpRequestRecord> httpRequestRecords = this.httpRequestRecordRepository
                .getAllForBrowserProfileAndDate(browserProfile, crawlDate);
        List<Metric> newMetrics = this.requestGraphService
                .createEntityRequestGraphAndGetMetrics(httpRequestRecords, browserProfile);
        List<Metric> oldMetrics = this.metricRepository
                .findAll(Example.of(Metric.from(crawlDate, null, null, RequestGraph.RequestGraphType.ENTITY_REQUEST_GRAPH, browserProfile)));

        this.metricRepository.delete(oldMetrics);
        this.metricRepository.save(newMetrics);
    }

    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getDomainGraphMetricForBrowserProfile(
            Metric.MetricType metricType
    ) {
        return getMetricsMatchingExample(
                Example.of(new Metric(null, null, metricType, RequestGraph.RequestGraphType.DOMAIN_REQUEST_GRAPH, null)));
    }

    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getDomainGraphMetrics() {
        return getMetricsMatchingExample(
                Example.of(new Metric(null, null, null, RequestGraph.RequestGraphType.DOMAIN_REQUEST_GRAPH, null)));
    }

    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getEntityGraphMetricForBrowserProfile(
            Metric.MetricType metricType
    ) {
        return getMetricsMatchingExample(
                Example.of(new Metric(null, null, metricType, RequestGraph.RequestGraphType.ENTITY_REQUEST_GRAPH, null)));
    }

    public Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getEntityGraphMetrics() {
        return getMetricsMatchingExample(
                Example.of(new Metric(null, null, null, RequestGraph.RequestGraphType.ENTITY_REQUEST_GRAPH, null)));
    }

    private Map<Metric.MetricType, Map<BrowserProfile, Map<String, Double>>> getMetricsMatchingExample(Example<Metric> example) {
        return this.metricRepository
                .findAll(example)
                .stream()
                .collect(Collectors.groupingBy(Metric::getMetricType,
                        Collectors.groupingBy(Metric::getBrowserProfile,
                                Collectors.toMap(Metric::getDate, Metric::getValue, (v1, v2) -> v1)
                        )));
    }

    public void storeDomainGraphMetricsForDate(Date crawlDate) {

        BrowserProfile.getAllBrowserProfiles()
                .forEach(browserProfile -> storeDomainGraphMetricsForProfileAndDate(browserProfile, crawlDate));
    }

    public void storeDomainGraphMetricsForProfileAndDate(
            BrowserProfile browserProfile,
            Date crawlDate
    ) {
        List<HttpRequestRecord> httpRequestRecords = this.httpRequestRecordRepository
                .getAllForBrowserProfileAndDate(browserProfile, crawlDate);
        if (httpRequestRecords.isEmpty()) {
            return;
        }

        List<Metric> newMetrics = this.requestGraphService
                .createDomainRequestGraphAndGetMetrics(httpRequestRecords, browserProfile);
        List<Metric> oldMetrics = this.metricRepository
                .findAll(Example.of(Metric.from(crawlDate, null, null, RequestGraph.RequestGraphType.DOMAIN_REQUEST_GRAPH, browserProfile)));

        this.metricRepository.delete(oldMetrics);
        this.metricRepository.save(newMetrics);
    }

    @Autowired
    public void setHttpRequestRecordRepository(HttpRequestRecordRepository httpRequestRecordRepository) {
        this.httpRequestRecordRepository = httpRequestRecordRepository;
    }

    @Autowired
    public void setMetricRepository(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Autowired
    public void setRequestGraphService(RequestGraphService requestGraphService) {
        this.requestGraphService = requestGraphService;
    }
}
