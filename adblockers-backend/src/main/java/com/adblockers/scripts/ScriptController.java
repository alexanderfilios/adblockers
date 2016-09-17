package com.adblockers.scripts;

import com.adblockers.firstparty.FirstParty;
import com.adblockers.firstparty.FirstPartyRepository;
import com.adblockers.utils.LegalEntity;
import com.adblockers.utils.Url;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("scripts/")
public class ScriptController {

    private FirstPartyRepository firstPartyRepository;
    private WhoisCollector whoisCollector;

    @RequestMapping("whois/firstparty")
    public Map<Url, LegalEntity> getWhoisInformationForAllFirstParties() {
        List<Url> urls = this.firstPartyRepository.findAll()
                .stream()
                .map(FirstParty::getUrl)
                .collect(Collectors.toList());
        return this.whoisCollector.findByUrl(urls);
    }

    @RequestMapping("whois/firstparty/{url}")
    public String getWhoisInformationForFirstParty(String url) throws MalformedURLException {
        return this.whoisCollector.findByUrl(Url.create(url)).toString();
    }

    @RequestMapping("whois/thirdparty")
    public void getWhoisInformationForThirdParties() {
//        List<Url> domains = this.firstPartyRepository.findAll()
//                .stream()
//                .limit(3)
//                .map(FirstParty::getUrl)
//                .collect(Collectors.toList());
        try {
            List<Url> domains = Arrays.asList(new Url[]{

                    Url.create("yahoo.com"),
                    Url.create("mkyong.com"),
                    Url.create("google.de"),
                    Url.create("facebook.com")});
            this.whoisCollector.findByUrl(domains);
//            return this.whoisCollector.findByUrl(domains);
        } catch (MalformedURLException e) {}
//        return null;
    }

    @Autowired
    public void setFirstPartyRepository(FirstPartyRepository firstPartyRepository) {
        this.firstPartyRepository = firstPartyRepository;
    }
    @Autowired
    public void setWhoisCollector(WhoisCollector whoisCollector) {
        this.whoisCollector = whoisCollector;
    }
}
