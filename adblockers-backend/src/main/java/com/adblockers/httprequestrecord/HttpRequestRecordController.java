package com.adblockers.httprequestrecord;

import com.adblockers.browserprofile.BrowserProfile;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("httprequestrecord")
public class HttpRequestRecordController {

    private HttpRequestRecordRepository httpRequestRecordRepository;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void saveForBrowserProfile(@RequestBody HttpRequestRecordDTO httpRequestDTO) {

        this.httpRequestRecordRepository.save(
                httpRequestDTO.toBrowserProfile(),
                httpRequestDTO.toHttpRequestRecord());
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
