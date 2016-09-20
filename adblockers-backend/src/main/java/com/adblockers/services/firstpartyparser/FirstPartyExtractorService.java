package com.adblockers.services.firstpartyparser;

import com.adblockers.entities.FirstParty;
import com.adblockers.entities.Url;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
@Service
public class FirstPartyExtractorService {

    public FirstPartyExtractorService() {}

    // We will crawl 500 top-ranked and 500 randomly-selected first parties
    private static final Integer TOP_FIRST_PARTIES = 10;
    private static final Integer RANDOM_FIRST_PARTIES = 10;
    // There are 1 million first parties in the list
    private static final Integer TOTAL_FIRST_PARTIES = 1000000;

    private LongCsvReaderService<FirstParty> longCsvReaderService;

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

    public Collection<FirstParty> extractFirstPartiesFromFile(String fileName) {
        // Find which websites to crawl
        Collection<Integer> ranksToCrawl = getRanksToCrawl();

        // Configure reader settings
        longCsvReaderService.setFilter(firstParty -> ranksToCrawl.contains(firstParty.getRank()));
        longCsvReaderService.setLineParser(line -> new FirstParty(Integer.parseInt(line[0]), line[1]));

        // Extract the first parties (just rank and URL) from the CSV file
        return longCsvReaderService.read(fileName)
                .stream()
                // Enhance with redirectionUrl
                .map(firstParty -> {
                    firstParty.setRedirectionDomain(findRedirectionUrlByUrl(firstParty.getDomain()).getUrl());
                    return firstParty;
                })
                .collect(Collectors.toList());
    }

    private Url findRedirectionUrlByUrl(Url url) {
        try {
            URL urlObject = new URL(url.getUrl());
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            // We have to get the header fields in order to find the redirection
            connection.getHeaderFields();
            return Url.create(connection.getURL().getHost());
        } catch (IOException e) {}
        return url;
    }

    @Autowired
    public void setLongCsvReaderService(LongCsvReaderService longCsvReaderService) {
        this.longCsvReaderService = longCsvReaderService;
    }
}
