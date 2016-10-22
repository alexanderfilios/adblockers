package com.adblockers.services;

import com.adblockers.controllers.LocationController;
import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.LegalEntityLocation;
import com.adblockers.entities.ServerLocation;
import com.adblockers.entities.Url;
import com.adblockers.repos.HttpRequestRecordRepository;
import com.adblockers.repos.LegalEntityLocationRepository;
import com.adblockers.repos.LegalEntityRepository;
import com.adblockers.repos.ServerLocationRepository;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.geoip.GeoIpCity;
import com.adblockers.services.whois.WhoisService;
import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.management.OperationsException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 22/10/16.
 */
@Service
public class LocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    private WhoisService whoisService;
    private GeoIpCity geoIpCity;
    private GeocodeService geocodeService;
    private HttpRequestRecordRepository httpRequestRecordRepository;
    private LegalEntityLocationRepository legalEntityLocationRepository;
    private LegalEntityRepository legalEntityRepository;
    private ServerLocationRepository serverLocationRepository;

    private static final Map<String, String> ISO_CODE_COUNTRY_MAP = Arrays.asList(Locale.getISOCountries()).stream()
            .collect(Collectors.toMap(isoCountry -> new Locale("", isoCountry).getDisplayCountry(), isoCountry -> isoCountry));

    public void runAllScriptsForStore() {
        this.storeWhoisInformationForThirdParties();
        try {
            this.storeGeocodeInformationForAllParties();
        } catch (OperationsException e) {}
        this.storeGeoIpInformationForAllThirdParties();
    }

    public void runAllScriptsForDelete() {
        this.deleteGeoIpInformationForAllParties();
        this.deleteGeocodeInformationForAllParties();
        this.deleteWhoisInformationForAllParties();
    }

    /**
     * Geocode service methods
     */

    public List<LegalEntityLocation> getGeocodeInformationForAllPartiesPerMarker() {
        return this.legalEntityLocationRepository.findAll();
    }

    public List<Map<String, String>> getGeocodeInformationForAllPartiesPerRegion() {
        Long totalLegalEntities = this.legalEntityLocationRepository.findAll().stream()
                .filter(legalEntityLocation -> !legalEntityLocation.isEmpty())
                .count();
        return this.serverLocationRepository.findAll().stream()
                .collect(Collectors.toMap(
                        serverLocation -> ISO_CODE_COUNTRY_MAP.getOrDefault(serverLocation.getCountry(), ""),
                        serverLocation -> 1,
                        (totalLocations, currentLocation) -> totalLocations + currentLocation
                )).entrySet().stream()
                .map(countryOccurrence -> ImmutableMap.<String, String>builder()
                        .put("country", countryOccurrence.getKey())
                        .put("occurrences", Double.valueOf((double) countryOccurrence.getValue() / totalLegalEntities).toString())
                        .build())
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getServerLocationStats() {
        Pair<Integer, Integer> stats = this.serverLocationRepository.findAll().stream()
                .reduce(new Pair<>(0, 0),
                        (accumulator, serverLocation) -> new Pair<>(
                                accumulator.getKey() + (serverLocation.isEmpty() ? 0 : 1),
                                accumulator.getValue() + 1),
                        (accumulator, current) -> new Pair<>(
                                accumulator.getKey() + current.getKey(),
                                accumulator.getValue() + current.getValue()));
        return ImmutableMap.of(
                "found", stats.getKey(),
                "total", stats.getValue()
        );
    }

    public Map<String, Integer> getLegalEntityLocationStats() {
        Pair<Integer, Integer> stats = this.legalEntityLocationRepository.findAll().stream()
                .reduce(new Pair<>(0, 0),
                        (accumulator, legalEntityLocation) -> new Pair<>(
                                accumulator.getKey() + (legalEntityLocation.isEmpty() ? 0 : 1),
                                accumulator.getValue() + 1),
                        (accumulator, current) -> new Pair<>(
                                accumulator.getKey() + current.getKey(),
                                accumulator.getValue() + current.getValue()
                        ));
        return ImmutableMap.of(
                "found", stats.getKey(),
                "total", stats.getValue()
        );
    }

    public void deleteGeocodeInformationForAllParties() {
        this.legalEntityLocationRepository.deleteAll();
    }

    /**
     * Looks for the {@link LegalEntityLocation}s using the Geocode APIs
     * There is an upper limit on the requests we can issue to the Geocode services
     * Hence, we have to spare on requests and only perform them for the legal entities
     * that do not have any geocode information stored yet
     * @throws OperationsException When no legal entities are found
     */
    public void storeGeocodeInformationForAllParties() throws OperationsException {
        Collection<LegalEntity> legalEntities = this.geocodeService
                .findLegalEntitiesWithoutGeocodeInformation();
        if (legalEntities.isEmpty()) {
            throw new OperationsException("No legal entities found. All entities have been located or there are no entities. Try invoking the whois service first.");
        }
        Collection<LegalEntityLocation> legalEntityLocations = this.geocodeService
                .findLocationsByLegalEntity(legalEntities);
        LOGGER.info("Storing " + legalEntityLocations.size() + " legal entity locations");
        this.legalEntityLocationRepository.save(legalEntityLocations);
    }

    /**
     * GeoIP service methods
     */

    public List<ServerLocation> getGeoIpInformationForAllPartiesPerMarker() {
        return this.serverLocationRepository.findAll();
    }

    public List<Map<String, String>> getGeoIpInformationForAllPartiesPerRegion() {
        Long totalServers = this.serverLocationRepository.findAll().stream()
                .filter(serverLocation -> !serverLocation.isEmpty())
                .count();
        return this.serverLocationRepository.findAll().stream()
                .collect(Collectors.toMap(
                        serverLocation -> ISO_CODE_COUNTRY_MAP.getOrDefault(serverLocation.getCountry(), ""),
                        serverLocation -> 1,
                        (totalLocations, currentLocation) -> totalLocations + currentLocation
                )).entrySet().stream()
                .map(countryOccurrence -> ImmutableMap.<String, String>builder()
                        .put("country", countryOccurrence.getKey())
                        .put("occurrences", Double.valueOf((double) countryOccurrence.getValue() / totalServers).toString())
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteGeoIpInformationForAllParties() {
        this.serverLocationRepository.deleteAll();
    }

    /**
     * Looks for the {@link ServerLocation}s using the GeoIP service
     * Records are refreshed, i.e. deleted and looked-up and stored anew
     */
    public void storeGeoIpInformationForAllThirdParties() {
        Set<Url> urls = this.httpRequestRecordRepository.getAllThirdPartyHosts();
        Set<ServerLocation> newServerLocations = geoIpCity.findServerLocationsByUrl(urls);

        LOGGER.info("Storing " + newServerLocations.size() + " server locations");

        this.serverLocationRepository.deleteAll();
        this.serverLocationRepository.save(newServerLocations);
    }

    /**
     * WHOIS service methods
     */

    public List<LegalEntity> getWhoisInformationForAllParties() {
        return this.legalEntityRepository.findAll();
    }

    public void deleteWhoisInformationForAllParties() {
        this.legalEntityRepository.deleteAll();
    }

    /**
     * Looks for new {@link LegalEntity}s by looking up in the WHOIS registry
     * Stores the new records and updates the existing ones
     * Updates occur for every field that was previously null and now was found non null
     * Even if the existing contact is already complete (has all fields filled), it will be looked up again
     */
    public void storeWhoisInformationForThirdParties() {
        Set<Url> urls = this.httpRequestRecordRepository.getAllThirdPartyDomains();
        Set<LegalEntity> legalEntities = this.whoisService.findLegalEntitiesByUrl(urls);

        LOGGER.info("Storing " + legalEntities.size() + " legal entities");

        this.legalEntityRepository.updateAndOverwriteByDomain(legalEntities);
    }

    @Autowired
    public void setWhoisService(WhoisService whoisService) {
        this.whoisService = whoisService;
    }

    @Autowired
    public void setGeoIpCity(GeoIpCity geoIpCity) {
        this.geoIpCity = geoIpCity;
    }

    @Autowired
    public void setHttpRequestRecordRepository(HttpRequestRecordRepository httpRequestRecordRepository) {
        this.httpRequestRecordRepository = httpRequestRecordRepository;
    }

    @Autowired
    public void setLegalEntityLocationRepository(LegalEntityLocationRepository legalEntityLocationRepository) {
        this.legalEntityLocationRepository = legalEntityLocationRepository;
    }

    @Autowired
    public void setLegalEntityRepository(LegalEntityRepository legalEntityRepository) {
        this.legalEntityRepository = legalEntityRepository;
    }

    @Autowired
    public void setServerLocationRepository(ServerLocationRepository serverLocationRepository) {
        this.serverLocationRepository = serverLocationRepository;
    }

    @Autowired
    public void setGeocodeService(GeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }
}
