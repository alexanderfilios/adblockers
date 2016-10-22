package com.adblockers.services.requestgraph;

import com.adblockers.entities.*;
import com.adblockers.repos.LegalEntityRepository;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
@Service
public class RequestGraphServiceImplementation implements RequestGraphService {

    private LegalEntityRepository legalEntityRepository;

    public RequestGraph<Url, LegalEntity> createEntityRequestGraph(@NotEmpty List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile) {
        Map<Url, LegalEntity> urlLegalEntityMap = this.legalEntityRepository.findAll().stream()
                .collect(Collectors.toMap(legalEntity -> legalEntity.getUrl(), legalEntity -> legalEntity));
        Set<Pair<Url, LegalEntity>> edges = httpRequestRecords.stream()
                .map(request -> Pair.of(request.getSourceDomain(), urlLegalEntityMap.getOrDefault(request.getTargetDomain(), null)))
                .collect(Collectors.toSet());
        Date crawlDate = httpRequestRecords.stream()
                .findAny()
                .map(record -> record.getCrawlDate())
                .orElse(null);

        return new RequestGraph(edges, crawlDate, browserProfile);
    }

    public RequestGraph<Url, Url> createDomainRequestGraph(@NotEmpty List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile) {
        Set<Pair<Url, Url>> edges = httpRequestRecords.stream()
                .map(request -> Pair.of(request.getSourceDomain(), request.getTargetDomain()))
                .collect(Collectors.toSet());
        Date crawlDate = httpRequestRecords.stream()
                .findAny()
                .map(record -> record.getCrawlDate())
                .orElse(null);
        return new RequestGraph(edges, crawlDate, browserProfile);
    }

    public List<Metric> createEntityRequestGraphAndGetMetrics(@NotEmpty List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile) {
        RequestGraph<Url, LegalEntity> entityRequestGraph = createEntityRequestGraph(httpRequestRecords, browserProfile);
        return Arrays.asList(Metric.MetricType.values()).stream()
                .map(metricType -> entityRequestGraph.getMetric(metricType))
                .collect(Collectors.toList());
    }
    public List<Metric> createDomainRequestGraphAndGetMetrics(@NotEmpty List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile) {
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
