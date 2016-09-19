package com.adblockers.services.requestgraph;

import com.adblockers.entities.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface RequestGraphService {
    RequestGraph<Url, LegalEntity> createEntityRequestGraph(@NotNull List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile);
    RequestGraph<Url, Url> createDomainRequestGraph(@NotNull List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile);
    List<Metric> createEntityRequestGraphAndGetMetrics(@NotNull List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile);
    List<Metric> createDomainRequestGraphAndGetMetrics(@NotNull List<HttpRequestRecord> httpRequestRecords, @NotNull BrowserProfile browserProfile);
}
