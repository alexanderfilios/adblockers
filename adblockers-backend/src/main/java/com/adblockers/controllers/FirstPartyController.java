package com.adblockers.controllers;

import com.adblockers.entities.FirstParty;
import com.adblockers.repos.FirstPartyRepository;
import com.adblockers.services.firstpartyparser.LongCsvReaderService;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@RestController
@CrossOrigin
@RequestMapping("firstparty")
public class FirstPartyController {

    // We will crawl 500 top-ranked and 500 randomly-selected first parties
    private static final Integer TOP_FIRST_PARTIES = 10;
    private static final Integer RANDOM_FIRST_PARTIES = 10;
    // There are 1 million first parties in the list
    private static final Integer TOTAL_FIRST_PARTIES = 1000000;

    private LongCsvReaderService<FirstParty> longCsvReaderService;
    private FirstPartyRepository firstPartyRepository;

    /**
     * We will crawl the top 500 websites and 500 further random websites.
     * @return A collection with the ranks
     */
    private Collection<Integer> getRanksToCrawl() {
        return Stream.concat(
                        ContiguousSet.create(Range.closed(1, TOP_FIRST_PARTIES), DiscreteDomain.integers()).stream(),
                        new Random().ints(RANDOM_FIRST_PARTIES, TOP_FIRST_PARTIES + 1, TOTAL_FIRST_PARTIES).boxed())
                .collect(Collectors.toList());
    }

    /**
     * This method is supposed to be invoked only once in the lifetime of the project.
     * It will parse the 1.000.000 websites from a file and extract them into the MongoDB.
     * @param fileName The csv file that contains the rank
     */
    @RequestMapping(value = {"/extract/{fileName}"}, method = RequestMethod.GET)
    public void extractFirstParties(
            @RequestParam String fileName
    ) {
        // Clear existing data
        this.firstPartyRepository.deleteAll();
        // Find which websites to crawl
        Collection<Integer> ranksToCrawl = getRanksToCrawl();

        // Configure reader settings
        longCsvReaderService.setFilter(firstParty -> ranksToCrawl.contains(firstParty.getRank()));
        longCsvReaderService.setLineParser(line -> new FirstParty(Integer.parseInt(line[0]), "http://www." + line[1]));

        // Store data
        this.firstPartyRepository.save(longCsvReaderService.read(fileName));
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
    public void setLongCsvReaderService(LongCsvReaderService<FirstParty> longCsvReaderService) {
        this.longCsvReaderService = longCsvReaderService;
    }
}
