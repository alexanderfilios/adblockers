package com.adblockers.services.requestgraph;

import com.adblockers.entities.*;
import com.adblockers.repos.LegalEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
@Component
public class RequestGraphServiceImplementation implements RequestGraphService {

    private LegalEntityRepository legalEntityRepository;

    public RequestGraph<Url, LegalEntity> createEntityRequestGraph(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile) {
        Map<Url, LegalEntity> urlLegalEntityMap = this.legalEntityRepository.findAll().stream()
                .collect(Collectors.toMap(legalEntity -> legalEntity.getUrl(), legalEntity -> legalEntity));
        Set<Pair<Url, LegalEntity>> edges = httpRequestRecords.stream()
                .map(request -> Pair.of(request.getSourceUrl(), urlLegalEntityMap.getOrDefault(request.getTargetUrl(), null)))
                .collect(Collectors.toSet());
        Date crawlDate = httpRequestRecords.stream()
                .findAny()
                .map(record -> record.getDate())
                .orElse(null);

        return new RequestGraph(edges, crawlDate, browserProfile);
    }

    public RequestGraph<Url, Url> createDomainRequestGraph(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile) {
        Set<Pair<Url, Url>> edges = httpRequestRecords.stream()
                .map(request -> Pair.of(request.getSourceUrl(), request.getSourceUrl()))
                .collect(Collectors.toSet());
        Date crawlDate = httpRequestRecords.stream()
                .findAny()
                .map(record -> record.getDate())
                .orElse(null);
        return new RequestGraph(edges, crawlDate, browserProfile);
    }

    public List<Metric> createEntityRequestGraphAndGetMetrics(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile) {
        RequestGraph<Url, LegalEntity> entityRequestGraph = createEntityRequestGraph(httpRequestRecords, browserProfile);
        return Arrays.asList(Metric.MetricType.values()).stream()
                .map(metricType -> entityRequestGraph.getMetric(metricType))
                .collect(Collectors.toList());
    }
    public List<Metric> createDomainRequestGraphAndGetMetrics(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile) {
        RequestGraph<Url, Url> domainRequestGraph = createDomainRequestGraph(httpRequestRecords, browserProfile);
        return Arrays.asList(Metric.MetricType.values()).stream()
                .map(metricType -> domainRequestGraph.getMetric(metricType))
                .collect(Collectors.toList());
    }

    @Autowired
    public void setLegalEntityRepository(LegalEntityRepository legalEntityRepository) {
        this.legalEntityRepository = legalEntityRepository;
    }
}
