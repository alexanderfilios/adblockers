package com.adblockers.controllers;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.LegalEntityLocation;
import com.adblockers.entities.ServerLocation;
import com.adblockers.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.management.OperationsException;
import java.util.*;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("scripts/")
public class LocationController {

    private LocationService locationService;

    @RequestMapping(value = {"runall"}, method = RequestMethod.PUT)
    public void runAllScriptsForStore() {
        this.locationService.runAllScriptsForStore();
    }

    @RequestMapping(value = {"runall"}, method = RequestMethod.DELETE)
    public void runAllScriptsForDelete() {
        this.locationService.runAllScriptsForDelete();
    }

    /**
     * Geocode service methods ({@link LegalEntityLocation}s)
     */

    @RequestMapping(value = {"geocode/all/markers"}, method = RequestMethod.GET)
    public List<LegalEntityLocation> getGeocodeInformationForAllPartiesPerMarker() {
        return this.locationService.getGeocodeInformationForAllPartiesPerMarker();
    }

    @RequestMapping(value = {"geocode/all/regions"}, method = RequestMethod.GET)
    public List<Map<String, String>> getGeocodeInformationForAllPartiesPerRegion() {
        return this.locationService.getGeocodeInformationForAllPartiesPerRegion();
    }

    @RequestMapping(value = {"geoip/stats"}, method = RequestMethod.GET)
    public Map<String, Integer> getServerLocationStats() {
        return this.locationService.getServerLocationStats();
    }

    @RequestMapping(value = {"geocode/stats"}, method = RequestMethod.GET)
    public Map<String, Integer> getLegalEntityLocationStats() {
        return this.locationService.getLegalEntityLocationStats();
    }


    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.DELETE)
    public void deleteGeocodeInformationForAllParties() {
        this.locationService.deleteGeocodeInformationForAllParties();
    }

    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.PUT)
    public void storeGeocodeInformationForAllParties() throws OperationsException {
        this.locationService.storeGeocodeInformationForAllParties();
    }

    /**
     * GeoIP service methods ({@link ServerLocation}s)
     */

    @RequestMapping(value = {"geoip/all/markers"}, method = RequestMethod.GET)
    public List<ServerLocation> getGeoIpInformationForAllPartiesPerMarker() {
        return this.locationService.getGeoIpInformationForAllPartiesPerMarker();
    }

    @RequestMapping(value = {"geoip/all/regions"}, method = RequestMethod.GET)
    public List<Map<String, String>> getGeoIpInformationForAllPartiesPerRegion() {
        return this.locationService.getGeoIpInformationForAllPartiesPerRegion();
    }

    @RequestMapping(value = {"geoip/all"}, method = RequestMethod.DELETE)
    public void deleteGeoIpInformationForAllParties() {
        this.locationService.deleteGeoIpInformationForAllParties();
    }

    @RequestMapping(value = {"geoip/thirdparties"}, method = RequestMethod.PUT)
    public void storeGeoIpInformationForAllThirdParties() {
        this.locationService.storeGeoIpInformationForAllThirdParties();
    }

    /**
     * WHOIS service methods
     */

    @RequestMapping(value = {"whois/all"}, method = RequestMethod.GET)
    public List<LegalEntity> getWhoisInformationForAllParties() {
        return this.locationService.getWhoisInformationForAllParties();
    }

    @RequestMapping(value = {"whois/all"}, method = RequestMethod.DELETE)
    public void deleteWhoisInformationForAllParties() {
        this.locationService.deleteWhoisInformationForAllParties();
    }

    @RequestMapping(value = {"whois/thirdparties"}, method = RequestMethod.PUT)
    public void storeWhoisInformationForThirdParties() {
        this.locationService.storeWhoisInformationForThirdParties();
    }

    @Autowired
    public void setLocationService(LocationService locationService) { this.locationService = locationService; }
}
