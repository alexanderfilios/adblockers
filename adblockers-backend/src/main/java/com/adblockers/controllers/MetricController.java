package com.adblockers.controllers;

import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.HttpRequestRecord;
import com.adblockers.entities.Metric;
import com.adblockers.repos.HttpRequestRecordRepository;
import com.adblockers.repos.MetricRepository;
import com.adblockers.services.requestgraph.RequestGraph;
import com.adblockers.services.requestgraph.RequestGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("metrics/")
public class MetricController {

    private HttpRequestRecordRepository httpRequestRecordRepository;
    private MetricRepository metricRepository;
    private RequestGraphService requestGraphService;

    @RequestMapping(value = {"entitygraph/{browserProfile}/{metricType}"}, method = RequestMethod.GET)
    public List<Metric> getEntityGraphMetricForBrowserProfile(
            @PathVariable Metric.MetricType metricType,
            @PathVariable BrowserProfile browserProfile
    ) {
        return this.metricRepository
                .findAll(Example.of(new Metric(null, null, metricType, RequestGraph.RequestGraphType.ENTITY_REQUEST_GRAPH, browserProfile)))
                .stream()
                .sorted((m1, m2) -> m1.getDate().compareTo(m2.getDate()))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = {"entitygraph/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeEntityGraphMetricsForDate(@PathVariable Date crawlDate) {
        BrowserProfile.getAllBrowserProfiles()
                .forEach(browserProfile -> storeEntityGraphMetricsForProfileAndDate(browserProfile, crawlDate));
    }

    @RequestMapping(value = {"entitygraph/{browserProfile}/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeEntityGraphMetricsForProfileAndDate(
            @PathVariable BrowserProfile browserProfile,
            @PathVariable Date crawlDate
    ) {
        List<HttpRequestRecord> httpRequestRecords = this.httpRequestRecordRepository
                .getAllForBrowserProfileAndDate(browserProfile, crawlDate);
        List<Metric> newMetrics = this.requestGraphService
                .createEntityRequestGraphAndGetMetrics(httpRequestRecords, browserProfile);
        List<Metric> oldMetrics = this.metricRepository
                .findAll(Example.of(new Metric(crawlDate, null, null, RequestGraph.RequestGraphType.ENTITY_REQUEST_GRAPH, browserProfile)));

        this.metricRepository.delete(oldMetrics);
        this.metricRepository.save(newMetrics);
    }

    @RequestMapping(value = {"domaingraph/{browserProfile}/{metricType}"}, method = RequestMethod.GET)
    public List<Metric> getDomainGraphMetricForBrowserProfile(
            @PathVariable Metric.MetricType metricType,
            @PathVariable BrowserProfile browserProfile
    ) {
        return this.metricRepository
                .findAll(Example.of(new Metric(null, null, metricType, RequestGraph.RequestGraphType.DOMAIN_REQUEST_GRAPH, browserProfile)))
                .stream()
                .sorted((m1, m2) -> m1.getDate().compareTo(m2.getDate()))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = {"domaingraph/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeDomainGraphMetricsForDate(@PathVariable Date crawlDate) {
        BrowserProfile.getAllBrowserProfiles()
                .forEach(browserProfile -> storeDomainGraphMetricsForProfileAndDate(browserProfile, crawlDate));
    }

    @RequestMapping(value = {"domaingraph/{browserProfile}/{crawlDate}"}, method = RequestMethod.PUT)
    public void storeDomainGraphMetricsForProfileAndDate(
            @PathVariable BrowserProfile browserProfile,
            @PathVariable Date crawlDate
    ) {
        List<HttpRequestRecord> httpRequestRecords = this.httpRequestRecordRepository
                .getAllForBrowserProfileAndDate(browserProfile, crawlDate);
        System.out.println(httpRequestRecords);

        List<Metric> newMetrics = this.requestGraphService
                .createDomainRequestGraphAndGetMetrics(httpRequestRecords, browserProfile);
        List<Metric> oldMetrics = this.metricRepository
                .findAll(Example.of(new Metric(crawlDate, null, null, RequestGraph.RequestGraphType.DOMAIN_REQUEST_GRAPH, browserProfile)));

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
