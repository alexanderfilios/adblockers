package com.adblockers.controllers;

import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.HttpRequestRecord;
import com.adblockers.entities.Metric;
import com.adblockers.repos.HttpRequestRecordRepository;
import com.adblockers.repos.MetricRepository;
import com.adblockers.services.requestgraph.RequestGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

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

    @RequestMapping({"entitygraph/{profileName}/{date}"})
    public void storeEntityGraphMetricsForProfileAndDate(
            @RequestParam("profileName") String profileName,
            @RequestParam("date") String date
    ) throws ParseException {
        BrowserProfile browserProfile = BrowserProfile.from(profileName);
        Date crawlDate = HttpRequestRecord.DATE_FORMAT.parse(date);

        List<HttpRequestRecord> httpRequestRecords = this.httpRequestRecordRepository
                .getAllForBrowserProfileAndDate(browserProfile, crawlDate);
        List<Metric> metrics = this.requestGraphService.createEntityRequestGraphAndGetMetrics(httpRequestRecords, browserProfile);
        this.metricRepository.save(metrics);
    }

    @RequestMapping({"domaingraph/{profileName}/{date}"})
    public void storeDomainGraphMetricsForProfileAndDate(
            @RequestParam("profileName") String profileName,
            @RequestParam("date") String date
    ) throws ParseException {
        BrowserProfile browserProfile = BrowserProfile.from(profileName);
        Date crawlDate = HttpRequestRecord.DATE_FORMAT.parse(date);

        List<HttpRequestRecord> httpRequestRecords = this.httpRequestRecordRepository
                .getAllForBrowserProfileAndDate(browserProfile, crawlDate);
        List<Metric> metrics = this.requestGraphService.createDomainRequestGraphAndGetMetrics(httpRequestRecords, browserProfile);
        this.metricRepository.save(metrics);
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
