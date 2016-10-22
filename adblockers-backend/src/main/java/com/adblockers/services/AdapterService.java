package com.adblockers.services;

import com.adblockers.entities.*;
import com.adblockers.repos.*;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.geoip.GeoIpService;
import com.adblockers.services.whois.WhoisService;
import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 22/10/16.
 */
@Service
public class AdapterService {

    private static Logger LOGGER = Logger.getLogger(AdapterService.class);

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
    public void convertLegalEntityLocations() {
        Map<Boolean, Map<String, LegalEntityLocation>> existingLegalEntityLocations = this.legalEntityLocationRepository.findAll().stream()
                .collect(Collectors.partitioningBy(LegalEntityLocation::isEmpty,
                        Collectors.toMap(LegalEntityLocation::getDomain, legalEntityLocation -> legalEntityLocation, (url1, url2) -> url1)));

        LOGGER.info(existingLegalEntityLocations.get(true).size() + " empty and "
                + existingLegalEntityLocations.get(false).size() + " non-empty Legal Entity Locations have already been detected.");

        this.legalEntityLocationAdapterRepository.findAll().stream()
                .map(LegalEntityLocationAdapter::getDomainUrl)
                .filter(url -> url != null)
                // Filter out the non-empty ServerLocations
                .filter(url -> !existingLegalEntityLocations.get(false).containsKey(url.getDomain()))
                .peek(url -> LOGGER.info("Fetching Legal Entity Location for " + url.getDomain()))
                // Calculate again the LegalEntityLocation
                .map(whoisService::findLegalEntityByUrl)
                .map(geocodeService::findLocationByLegalEntity)
                // Do not save the ones that are found to be empty again
                .filter(legalEntityLocation -> !(legalEntityLocation.isEmpty() && existingLegalEntityLocations.get(true).containsKey(legalEntityLocation.getUrl().getDomain())))
                // Find the existing (empty) ServerLocation to update or otherwise insert the new one
                .forEach(legalEntityLocation -> legalEntityLocationRepository.save(legalEntityLocation));
    }

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
    public void convertServerLocations() {
        Map<Boolean, Map<String, ServerLocation>> existingServerLocations = this.serverLocationRepository.findAll().stream()
                .collect(Collectors.partitioningBy(ServerLocation::isEmpty,
                        Collectors.toMap(ServerLocation::getDomain, serverLocation -> serverLocation, (url1, url2) -> url1)));

        LOGGER.info(existingServerLocations.get(true).size() + " empty and "
                + existingServerLocations.get(false).size() + " non-empty Server Locations have already been detected.");

        this.legalEntityLocationAdapterRepository.findAll().stream()
                .map(LegalEntityLocationAdapter::getDomainUrl)
                .filter(url -> url != null)
                // Filter out the non-empty ServerLocations
                .filter(url -> !existingServerLocations.get(false).containsKey(url.getDomain()))
                .peek(url -> LOGGER.info("Fetching Server Location for " + url.getDomain()))
                // Calculate again the ServerLocation
                .map(geoIpService::findServerLocationByUrl)
                // Do not save the ones that are found to be empty again
                .filter(serverLocation -> !(serverLocation.isEmpty() && existingServerLocations.get(true).containsKey(serverLocation.getUrl().getDomain())))
                // Find the existing (empty) ServerLocation to update or otherwise insert the new one
                .forEach(serverLocation -> serverLocationRepository.save(
                        existingServerLocations.get(true).getOrDefault(serverLocation.getDomain(), serverLocation)));
    }

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
