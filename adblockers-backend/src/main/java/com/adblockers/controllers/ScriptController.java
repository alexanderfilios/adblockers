package com.adblockers.controllers;

import com.adblockers.entities.*;
import com.adblockers.repos.FirstPartyRepository;
import com.adblockers.repos.HttpRequestRecordRepository;
import com.adblockers.services.geoip.GeoIpCity;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.whois.WhoisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private FirstPartyRepository firstPartyRepository;
    private HttpRequestRecordRepository httpRequestRecordRepository;

    @RequestMapping("geocode/firstparties")
    public List<LegalEntityLocation> getGeocodeInformationForAllFirstParties() {
        List<LegalEntity> legalEntities = this.getWhoisInformationForAllFirstParties();
        return this.geocodeService.findLocationsByLegalEntity(legalEntities);
    }
    @RequestMapping("geocode/thirdparties")
    public List<LegalEntityLocation> getGeocodeInformationForAllThirdParties() {
        List<LegalEntity> legalEntities = this.getWhoisInformationForThirdParties();
        return this.geocodeService.findLocationsByLegalEntity(legalEntities);
    }

    @RequestMapping("geoip/firstparties")
    public List<ServerLocation> getGeoIpInformationForAllFirstParties() {
        List<Url> urls = this.firstPartyRepository.findAll().stream()
                .map(FirstParty::getUrl)
                .collect(Collectors.toList());
        return geoIpCity.findServerLocationsByUrl(urls);
    }

    @RequestMapping("geoip/thirdparties")
    public List<ServerLocation> getGeoIpInformationForAllThirdParties() {
        List<Url> urls = this.httpRequestRecordRepository.getAllThirdParties();
        return geoIpCity.findServerLocationsByUrl(urls);
    }

    @RequestMapping("whois/firstparties")
    public List<LegalEntity> getWhoisInformationForAllFirstParties() {
        List<Url> urls = this.firstPartyRepository.findAll()
                .stream()
                .map(FirstParty::getUrl)
                .collect(Collectors.toList());
        return this.whoisService.findLegalEntitiesByUrl(urls);
    }

    @RequestMapping("whois/thirdparties")
    public List<LegalEntity> getWhoisInformationForThirdParties() {
        List<Url> urls = this.httpRequestRecordRepository.getAllThirdParties();
        return this.whoisService.findLegalEntitiesByUrl(urls);
    }

    @Autowired
    public void setFirstPartyRepository(FirstPartyRepository firstPartyRepository) {
        this.firstPartyRepository = firstPartyRepository;
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
    public void setGeocodeService(GeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }
}
