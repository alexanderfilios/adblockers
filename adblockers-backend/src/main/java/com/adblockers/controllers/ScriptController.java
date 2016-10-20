package com.adblockers.controllers;

import com.adblockers.entities.*;
import com.adblockers.repos.*;
import com.adblockers.services.geoip.GeoIpCity;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.shellscript.ScriptExecutor;
import com.adblockers.services.whois.WhoisService;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.management.OperationsException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("scripts/")
public class ScriptController {

    private static final Logger LOGGER = Logger.getLogger(ScriptController.class);

    private WhoisService whoisService;
    private GeoIpCity geoIpCity;
    private GeocodeService geocodeService;
    private HttpRequestRecordRepository httpRequestRecordRepository;
    private LegalEntityLocationRepository legalEntityLocationRepository;
    private LegalEntityRepository legalEntityRepository;
    private ServerLocationRepository serverLocationRepository;

    private static final Map<String, String> ISO_CODE_COUNTRY_MAP = Arrays.asList(Locale.getISOCountries()).stream()
            .collect(Collectors.toMap(isoCountry -> new Locale("", isoCountry).getDisplayCountry(), isoCountry -> isoCountry));

    @RequestMapping(value = {"runall"}, method = RequestMethod.PUT)
    public void runAllScriptsForStore() {
        this.storeWhoisInformationForThirdParties();
        try {
            this.storeGeocodeInformationForAllParties();
        } catch (OperationsException e) {}
        this.storeGeoIpInformationForAllThirdParties();
    }

    @RequestMapping(value = {"runall"}, method = RequestMethod.DELETE)
    public void runAllScriptsForDelete() {
        this.deleteGeoIpInformationForAllParties();
        this.deleteGeocodeInformationForAllParties();
        this.deleteWhoisInformationForAllParties();
    }

    /**
     * Geocode service methods
     */

    @RequestMapping(value = {"geocode/all/markers"}, method = RequestMethod.GET)
    public List<LegalEntityLocation> getGeocodeInformationForAllPartiesPerMarker() {
        return this.legalEntityLocationRepository.findAll();
    }

    @RequestMapping(value = {"geocode/all/regions"}, method = RequestMethod.GET)
    public List<Map<String, String>> getGeocodeInformationForAllPartiesPerRegion() {
        Integer totalLegalEntities = this.legalEntityLocationRepository.findAll().size();
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


    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.DELETE)
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
    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.PUT)
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

    @RequestMapping(value = {"geoip/all/markers"}, method = RequestMethod.GET)
    public List<ServerLocation> getGeoIpInformationForAllPartiesPerMarker() {
        return this.serverLocationRepository.findAll();
    }

    @RequestMapping(value = {"geoip/all/regions"}, method = RequestMethod.GET)
    public List<Map<String, String>> getGeoIpInformationForAllPartiesPerRegion() {
        Integer totalServers = this.serverLocationRepository.findAll().size();
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

    @RequestMapping(value = {"geoip/all"}, method = RequestMethod.DELETE)
    public void deleteGeoIpInformationForAllParties() {
        this.serverLocationRepository.deleteAll();
    }

    /**
     * Looks for the {@link ServerLocation}s using the GeoIP service
     * Records are refreshed, i.e. deleted and looked-up and stored anew
     */
    @RequestMapping(value = {"geoip/thirdparties"}, method = RequestMethod.PUT)
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

    @RequestMapping(value = {"whois/all"}, method = RequestMethod.GET)
    public List<LegalEntity> getWhoisInformationForAllParties() {
        return this.legalEntityRepository.findAll();
    }

    @RequestMapping(value = {"whois/all"}, method = RequestMethod.DELETE)
    public void deleteWhoisInformationForAllParties() {
        this.legalEntityRepository.deleteAll();
    }

    /**
     * Looks for new {@link LegalEntity}s by looking up in the WHOIS registry
     * Stores the new records and updates the existing ones
     * Updates occur for every field that was previously null and now was found non null
     * Even if the existing contact is already complete (has all fields filled), it will be looked up again
     */
    @RequestMapping(value = {"whois/thirdparties"}, method = RequestMethod.PUT)
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
