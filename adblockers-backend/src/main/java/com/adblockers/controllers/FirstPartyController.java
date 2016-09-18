package com.adblockers.controllers;

import com.adblockers.entities.FirstParty;
import com.adblockers.repos.FirstPartyRepository;
import com.adblockers.services.firstpartyparser.FirstPartyExtractorService;
import com.adblockers.services.firstpartyparser.LongCsvReaderService;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@RestController
@CrossOrigin
@RequestMapping("firstparty")
public class FirstPartyController {

    private FirstPartyExtractorService firstPartyExtractorService;
    private FirstPartyRepository firstPartyRepository;

    /**
     * This method is supposed to be invoked only once in the lifetime of the project.
     * It will parse the 1.000.000 websites from a file and extract them into the MongoDB.
     * @param fileName The csv file that contains the rank
     */
    @RequestMapping(value = {"/extract/{fileName}"}, method = RequestMethod.GET)
    public void extractFirstParties(@RequestParam String fileName) {
        this.firstPartyRepository.deleteAll();
        Collection<FirstParty> firstParties = this.firstPartyExtractorService.extractFirstPartiesFromFile(fileName);
        this.firstPartyRepository.save(firstParties);
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public void deleteFirstParties() {
        this.firstPartyRepository.deleteAll();
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long countFirstParties() {
        return this.firstPartyRepository.count();
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Collection<?> getFirstParties() {
        return this.firstPartyRepository.findAll()
                .stream()
                .map(firstParty -> ImmutableMap.<String, Object>builder()
                        .put("rank", firstParty.getRank())
                        .put("url", firstParty.getUrl().getUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Autowired
    public void setFirstPartyRepository(FirstPartyRepository firstPartyRepository) {
        this.firstPartyRepository = firstPartyRepository;
    }

    @Autowired
    public void setFirstPartyExtractorService(FirstPartyExtractorService firstPartyExtractorService) {
        this.firstPartyExtractorService = firstPartyExtractorService;
    }
}
