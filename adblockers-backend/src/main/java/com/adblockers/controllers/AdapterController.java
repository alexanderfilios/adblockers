package com.adblockers.controllers;

import com.adblockers.entities.*;
import com.adblockers.repos.*;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.geoip.GeoIpService;
import com.adblockers.services.whois.WhoisService;
import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 20/10/16.
 */
@CrossOrigin
@RestController
@RequestMapping("adapters/")
public class AdapterController {

    private static Logger LOGGER = Logger.getLogger(AdapterController.class);

    private MetricAdapterRepository metricAdapterRepository;
    private MetricRepository metricRepository;
    private LegalEntityLocationAdapterRepository legalEntityLocationAdapterRepository;
    private LegalEntityLocationRepository legalEntityLocationRepository;
    private ServerLocationRepository serverLocationRepository;

    private WhoisService whoisService;
    private GeocodeService geocodeService;
    private GeoIpService geoIpService;

    /**
     * Converts all legacy data regarding the metrics to {@link Metric} objects and stores them
     */
    @RequestMapping(value = {"metrics"}, method = RequestMethod.PUT)
    public void convertMetrics() {
        List<Metric> metrics = this.metricAdapterRepository.findAll().stream()
                .map(MetricAdapter::toMetric)
                .filter(metric -> !metric.getMetricType().equals(Metric.MetricType.UNDEFINED))
                .collect(Collectors.toList());

        this.metricRepository.deleteAll();
        this.metricRepository.save(metrics);
    }

    /**
     * Converts all legacy data regarding entity location to {@link ServerLocation} objects and stores them
     */
    @RequestMapping(value = {"legalentities"}, method = RequestMethod.PUT)
    public void convertLegalEntityLocations() {
        Map<String, Boolean> existingLegalEntityLocations = this.legalEntityLocationRepository.findAll().stream()
                .collect(Collectors.toMap(LegalEntityLocation::getDomain, (legalEntityLocation) -> true, (url1, url2) -> url1));

        this.legalEntityLocationAdapterRepository.findAll().stream()
                .map(LegalEntityLocationAdapter::getDomainUrl)
                .filter(url -> url != null)
                .filter(url -> !existingLegalEntityLocations.containsKey(url.getDomain()))
                .peek(url -> LOGGER.info("Fetching Legal Entity Location for " + url.getDomain()))
                .map(whoisService::findLegalEntityByUrl)
                .map(geocodeService::findLocationByLegalEntity)
                .forEach(legalEntityLocation -> legalEntityLocationRepository.save(legalEntityLocation));
    }

    @RequestMapping(value = {"legalentities/stats"}, method = RequestMethod.GET)
    public Map<String, Integer> getLegalEntityLocationStats() {
        Pair<Integer, Integer> stats = this.legalEntityLocationAdapterRepository.findAll().stream()
                .reduce(new Pair<>(0, 0),
                        (accumulator, legalEntityLocation) -> new Pair<>(
                                accumulator.getKey() + (legalEntityLocation.isLegalEntityLocationEmpty() ? 0 : 1),
                                accumulator.getValue() + 1),
                        (accumulator, current) -> new Pair<>(
                                accumulator.getKey() + current.getKey(),
                                accumulator.getValue() + current.getValue()));
        return ImmutableMap.of(
                "found", stats.getKey(),
                "total", stats.getValue()
        );
    }

    /**
     * Converts all legacy data regarding server (entity) location to {@link ServerLocation} objects and stores them
     */
    @RequestMapping(value = {"servers"}, method = RequestMethod.PUT)
    public void convertServerLocations() {
        Map<String, Boolean> existingServerLocations = this.serverLocationRepository.findAll().stream()
                .collect(Collectors.toMap(ServerLocation::getDomain, serverLocation -> true, (url1, url2) -> url1));

        this.legalEntityLocationAdapterRepository.findAll().stream()
                .map(LegalEntityLocationAdapter::getDomainUrl)
                .filter(url -> url != null)
                .filter(url -> !existingServerLocations.containsKey(url.getDomain()))
                .peek(url -> LOGGER.info("Fetching Server Location for " + url.getDomain()))
                .map(geoIpService::findServerLocationByUrl)
                .forEach(serverLocation -> serverLocationRepository.save(serverLocation));
    }

    @RequestMapping(value = {"servers/stats"}, method = RequestMethod.GET)
    public Map<String, Integer> getServerLocationStats() {
        Pair<Integer, Integer> stats = this.legalEntityLocationAdapterRepository.findAll().stream()
                .reduce(new Pair<>(0, 0),
                        (accumulator, legalEntityLocation) -> new Pair<>(
                                accumulator.getKey() + (legalEntityLocation.isServerLocationEmpty() ? 0 : 1),
                                accumulator.getValue() + 1),
                        (accumulator, current) -> new Pair<>(
                                accumulator.getKey() + current.getKey(),
                                accumulator.getValue() + current.getValue()));
        return ImmutableMap.of(
                "found", stats.getKey(),
                "total", stats.getValue()
        );
    }

    @Autowired
    public void setMetricAdapterRepository(MetricAdapterRepository metricAdapterRepository) { this.metricAdapterRepository = metricAdapterRepository; }

    @Autowired
    public void setMetricRepository(MetricRepository metricRepository) { this.metricRepository = metricRepository; }

    @Autowired
    public void setLegalEntityLocationAdapterRepository(LegalEntityLocationAdapterRepository legalEntityLocationAdapterRepository) { this.legalEntityLocationAdapterRepository = legalEntityLocationAdapterRepository; }

    @Autowired
    public void setGeocodeService(GeocodeService geocodeService) { this.geocodeService = geocodeService; }

    @Autowired
    public void setWhoisService(WhoisService whoisService) { this.whoisService = whoisService; }

    @Autowired
    public void setGeoIpService(GeoIpService geoIpService) { this.geoIpService = geoIpService; }

    @Autowired
    public void setLegalEntityLocationRepository(LegalEntityLocationRepository legalEntityLocationRepository) { this.legalEntityLocationRepository = legalEntityLocationRepository; }

    @Autowired
    public void setServerLocationRepository(ServerLocationRepository serverLocationRepository) { this.serverLocationRepository = serverLocationRepository; }
}
