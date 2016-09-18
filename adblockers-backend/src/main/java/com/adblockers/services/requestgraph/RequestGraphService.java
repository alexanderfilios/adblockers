package com.adblockers.services.requestgraph;

import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.HttpRequestRecord;
import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.Url;

import java.util.List;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface RequestGraphService {
    RequestGraph<Url, LegalEntity> createEntityRequestGraph(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile);
    RequestGraph<Url, Url> createDomainRequestGraph(List<HttpRequestRecord> httpRequestRecords, BrowserProfile browserProfile);
}
