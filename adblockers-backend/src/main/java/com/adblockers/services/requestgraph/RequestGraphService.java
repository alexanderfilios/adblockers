package com.adblockers.services.requestgraph;

import com.adblockers.entities.*;

import java.util.List;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface RequestGraphService {
    RequestGraph<Url, LegalEntity> createEntityRequestGraph(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile);
    RequestGraph<Url, Url> createDomainRequestGraph(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile);
    List<Metric> createEntityRequestGraphAndGetMetrics(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile);
    List<Metric> createDomainRequestGraphAndGetMetrics(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile);
}
