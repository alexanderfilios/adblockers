package com.adblockers.controllers;

import com.adblockers.transfer.HttpRequestRecordDTO;
import com.adblockers.repos.HttpRequestRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@CrossOrigin
@RestController
@RequestMapping("httprequestrecord")
public class HttpRequestRecordController {

    private HttpRequestRecordRepository httpRequestRecordRepository;

    @RequestMapping(value = "/all", method = RequestMethod.DELETE)
    public void clearAllDataCollections() {
        this.httpRequestRecordRepository.remove(null);
    }

    @RequestMapping(value = "/bydate/{date}", method = RequestMethod.DELETE)
    public void clearDataCollectionsByDate(@PathVariable Date crawlDate) {
        this.httpRequestRecordRepository.remove(crawlDate);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void saveForBrowserProfile(
            @RequestBody HttpRequestRecordDTO httpRequestDTO) {

        this.httpRequestRecordRepository.save(
                httpRequestDTO.toBrowserProfile(),
                httpRequestDTO.toHttpRequestRecord());
    }

    @Autowired
    public void setHttpRequestRecordRepository(HttpRequestRecordRepository httpRequestRecordRepository) {
        this.httpRequestRecordRepository = httpRequestRecordRepository;
    }
}
