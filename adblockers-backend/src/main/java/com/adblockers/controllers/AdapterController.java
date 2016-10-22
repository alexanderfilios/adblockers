package com.adblockers.controllers;

import com.adblockers.services.AdapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by alexandrosfilios on 20/10/16.
 */
@CrossOrigin
@RestController
@RequestMapping("adapters/")
public class AdapterController {

    private AdapterService adapterService;

    @RequestMapping(value = {"metrics"}, method = RequestMethod.PUT)
    public void convertMetrics() {
        this.adapterService.convertMetrics();
    }

    @RequestMapping(value = {"legalentities"}, method = RequestMethod.PUT)
    public void convertLegalEntityLocations() {
        this.adapterService.convertLegalEntityLocations();
    }

    @RequestMapping(value = {"legalentities/stats"}, method = RequestMethod.GET)
    public Map<String, Integer> getLegalEntityLocationStats() {
        return this.adapterService.getLegalEntityLocationStats();
    }

    @RequestMapping(value = {"servers"}, method = RequestMethod.PUT)
    public void convertServerLocations() {
        this.adapterService.convertServerLocations();
    }

    @RequestMapping(value = {"servers/stats"}, method = RequestMethod.GET)
    public Map<String, Integer> getServerLocationStats() {
        return this.adapterService.getServerLocationStats();
    }

    @Autowired
    public void setAdapterService(AdapterService adapterService) { this.adapterService = adapterService; }
}
