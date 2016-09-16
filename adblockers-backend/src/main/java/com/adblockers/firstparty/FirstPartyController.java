package com.adblockers.firstparty;

import com.adblockers.parser.LongCsvReader;
import com.google.common.collect.*;
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

    private FirstPartyRepository firstPartyRepository;

    /**
     * We will crawl the top 500 websites and 500 further random websites.
     * @return A collection with the ranks
     */
    private Collection<Integer> getRanksToCrawl() {
        return Stream.concat(
                        ContiguousSet.create(Range.closed(1, 10), DiscreteDomain.integers()).stream(),
                        new Random().ints(10, 501, 1000000).boxed())
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
        LongCsvReader<FirstParty> longCsvReader = new LongCsvReader(fileName);
        longCsvReader.setFilter(firstParty -> ranksToCrawl.contains(firstParty.getRank()));
        longCsvReader.setLineParser(line -> new FirstParty(Integer.parseInt(line[0]), "http://www." + line[1]));

        // Store data
        this.firstPartyRepository.save(longCsvReader.read());
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
}
