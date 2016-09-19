package com.adblockers.controllers;

import com.adblockers.entities.*;
import com.adblockers.repos.*;
import com.adblockers.services.geoip.GeoIpCity;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.whois.WhoisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.management.OperationsException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("scripts/")
public class ScriptController {

    private WhoisService whoisService;
    private GeoIpCity geoIpCity;
    private GeocodeService geocodeService;
    private HttpRequestRecordRepository httpRequestRecordRepository;
    private LegalEntityLocationRepository legalEntityLocationRepository;
    private LegalEntityRepository legalEntityRepository;
    private ServerLocationRepository serverLocationRepository;

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

    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.GET)
    public List<LegalEntityLocation> getGeocodeInformationForAllParties() {
        return this.legalEntityLocationRepository.findAll();
    }

    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.DELETE)
    public void deleteGeocodeInformationForAllParties() {
        this.legalEntityLocationRepository.deleteAll();
    }

    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.PUT)
    public void storeGeocodeInformationForAllParties() throws OperationsException {
        List<LegalEntity> legalEntities = this.getWhoisInformationForAllParties();
        if (legalEntities.isEmpty()) {
            throw new OperationsException("No legal entities found. Try invoking the whois service first.");
        }
        Collection<LegalEntityLocation> legalEntityLocations = this.geocodeService.findLocationsByLegalEntity(legalEntities);
        this.legalEntityLocationRepository.save(legalEntityLocations);
    }

    /**
     * GeoIP service methods
     */

    @RequestMapping(value = {"geoip/all"}, method = RequestMethod.GET)
    public List<ServerLocation> getGeoIpInformationForAllParties() {
        return this.serverLocationRepository.findAll();
    }

    @RequestMapping(value = {"geoip/all"}, method = RequestMethod.DELETE)
    public void deleteGeoIpInformationForAllParties() {
        this.serverLocationRepository.deleteAll();
    }

    @RequestMapping(value = {"geoip/thirdparties"}, method = RequestMethod.PUT)
    public void storeGeoIpInformationForAllThirdParties() {
        Set<Url> urls = this.httpRequestRecordRepository.getAllThirdPartyHosts();
        Set<ServerLocation> newServerLocations = geoIpCity.findServerLocationsByUrl(urls);

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

    @RequestMapping(value = {"whois/thirdparties"}, method = RequestMethod.PUT)
    public void storeWhoisInformationForThirdParties() {
        Set<Url> urls = this.httpRequestRecordRepository.getAllThirdPartyDomains();
        Set<LegalEntity> legalEntities = this.whoisService.findLegalEntitiesByUrl(urls);
        this.legalEntityRepository.save(legalEntities);
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
