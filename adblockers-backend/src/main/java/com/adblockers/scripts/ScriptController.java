package com.adblockers.scripts;

import com.adblockers.firstparty.FirstParty;
import com.adblockers.firstparty.FirstPartyRepository;
import com.adblockers.httprequestrecord.HttpRequestRecordRepository;
import com.adblockers.utils.LegalEntity;
import com.adblockers.utils.ServerLocation;
import com.adblockers.utils.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
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
    private GeoIpService geoIpService;
    private FirstPartyRepository firstPartyRepository;
    private HttpRequestRecordRepository httpRequestRecordRepository;

    @RequestMapping("geoip/firstparty")
    public List<ServerLocation> getGeoIpInformationForAllFirstParties() {
        List<Url> urls = this.firstPartyRepository.findAll().stream()
                .map(FirstParty::getUrl)
                .collect(Collectors.toList());
        return geoIpService.getServerLocationsByUrl(urls);
    }

    @RequestMapping("geoip/thirdparty")
    public List<ServerLocation> getGeoIpInformationForAllThirdParties() {
        List<Url> urls = this.httpRequestRecordRepository.getAllThirdParties();
        return geoIpService.getServerLocationsByUrl(urls);
    }

    @RequestMapping("whois/firstparty")
    public List<LegalEntity> getWhoisInformationForAllFirstParties() {
        List<Url> urls = this.firstPartyRepository.findAll()
                .stream()
                .map(FirstParty::getUrl)
                .collect(Collectors.toList());
        return this.whoisService.findLegalEntitiesByUrl(urls);
    }

    @RequestMapping("whois/thirdparty")
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
    public void setGeoIpService(GeoIpService geoIpService) {
        this.geoIpService = geoIpService;
    }

    @Autowired
    public void setHttpRequestRecordRepository(HttpRequestRecordRepository httpRequestRecordRepository) {
        this.httpRequestRecordRepository = httpRequestRecordRepository;
    }
}
