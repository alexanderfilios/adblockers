package com.adblockers.httprequestrecord;

import com.adblockers.browserprofile.BrowserProfile;
import com.adblockers.browserprofile.BrowserProfileDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@RestController
@RequestMapping("httprequestrecord")
public class HttpRequestRecordController implements InitializingBean {

    private HttpRequestRecordRepository httpRequestRecordRepository;

    @Override
    public void afterPropertiesSet() {
        // Populate collection with some data
        BrowserProfile browserProfile = BrowserProfile.from("GHOSTERY", true, true);
        if (this.httpRequestRecordRepository.count(browserProfile) == 0) {
            this.httpRequestRecordRepository.save(browserProfile, new HttpRequestRecord("http://www.google.com", "http://www.youtube.com"));
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void saveForBrowserProfile(
            @RequestParam BrowserProfileDTO browserProfileDTO,
            @RequestParam List<HttpRequestRecordDTO> httpRequestRecordDTOs) {

        this.httpRequestRecordRepository.save(
                browserProfileDTO.toBrowserProfile(),
                httpRequestRecordDTOs.stream()
                        .map(HttpRequestRecordDTO::toHttpRequestRecord)
                        .collect(Collectors.toList()));
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<?> getAllForBrowserProfile(
            String adblocker,
            Boolean defaultProtection,
            Boolean mobileUserAgent
    ) {
        return this.httpRequestRecordRepository.getAllForBrowserProfile(BrowserProfile.from("GHOSTERY", true, true));
    }

    @Autowired
    public void setHttpRequestRecordRepository(HttpRequestRecordRepository httpRequestRecordRepository) {
        this.httpRequestRecordRepository = httpRequestRecordRepository;
    }
}
