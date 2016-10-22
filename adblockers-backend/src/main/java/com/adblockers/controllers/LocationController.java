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
     * Geocode service methods
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

    /**
     * Looks for the {@link LegalEntityLocation}s using the Geocode APIs
     * There is an upper limit on the requests we can issue to the Geocode services
     * Hence, we have to spare on requests and only perform them for the legal entities
     * that do not have any geocode information stored yet
     * @throws OperationsException When no legal entities are found
     */
    @RequestMapping(value = {"geocode/all"}, method = RequestMethod.PUT)
    public void storeGeocodeInformationForAllParties() throws OperationsException {
        this.locationService.storeGeocodeInformationForAllParties();
    }

    /**
     * GeoIP service methods
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

    /**
     * Looks for the {@link ServerLocation}s using the GeoIP service
     * Records are refreshed, i.e. deleted and looked-up and stored anew
     */
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

    /**
     * Looks for new {@link LegalEntity}s by looking up in the WHOIS registry
     * Stores the new records and updates the existing ones
     * Updates occur for every field that was previously null and now was found non null
     * Even if the existing contact is already complete (has all fields filled), it will be looked up again
     */
    @RequestMapping(value = {"whois/thirdparties"}, method = RequestMethod.PUT)
    public void storeWhoisInformationForThirdParties() {
        this.locationService.storeWhoisInformationForThirdParties();
    }

    @Autowired
    public void setLocationService(LocationService locationService) { this.locationService = locationService; }
}
