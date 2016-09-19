package com.adblockers.repos;

import com.adblockers.entities.BrowserProfile;
import com.adblockers.entities.HttpRequestRecord;
import com.adblockers.entities.Url;
import com.sun.istack.internal.Nullable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.core.query.Query;

import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@Repository
public class HttpRequestRecordRepository {

    private MongoTemplate mongoTemplate;
    private static final Logger LOGGER = Logger.getLogger(HttpRequestRecordRepository.class);

    /**
     * Stores a {@link HttpRequestRecord}
     * @param browserProfile The {@link BrowserProfile} that sent the request
     * @param httpRequestRecord The {@link HttpRequestRecord} to store
     */
    public void save(BrowserProfile browserProfile, HttpRequestRecord httpRequestRecord) {
        this.mongoTemplate.save(httpRequestRecord, browserProfile.toCollectionName());
    }

    /**
     * Returns all {@link HttpRequestRecord}s for a {@link BrowserProfile} and a {@link Date}
     * @param browserProfile The {@link BrowserProfile}
     * @param date The {@link Date}
     * @return The {@link HttpRequestRecord}s
     */
    public List<HttpRequestRecord> getAllForBrowserProfileAndDate(@NotNull BrowserProfile browserProfile, @NotNull Date date) {

        Query query = Query.query(Criteria.where("crawlDate")
                .is(HttpRequestRecord.DATE_FORMAT.format(date)));
        return this.mongoTemplate.find(query, HttpRequestRecord.class, browserProfile.toCollectionName());
    }

    /**
     * Returns all unique third party domains
     * @return The {@link Url}s of the third parties
     */
    public Set<Url> getAllThirdPartyDomains() {
        return getAllThirdPartyDomainsForDate(null);
    }

    /**
     * Returns all unique third party domains
     * @param date The {@link Date}. If null, all existing dates are examined
     * @return A {@link Set} of the third-party {@link Url}s
     */
    public Set<Url> getAllThirdPartyDomainsForDate(@Nullable Date date) {
        return BrowserProfile.getAllBrowserProfiles().stream()
                .flatMap(browserProfile -> getAllThirdPartyDomainsForBrowserProfileAndDate(browserProfile, date).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Returns all unique third party domains
     * @param browserProfile The {@link BrowserProfile}
     * @param date The {@link Date}
     * @return A {@link Set} with the {@link Url}s containing the domains
     */
    public Set<Url> getAllThirdPartyDomainsForBrowserProfileAndDate(@NotNull BrowserProfile browserProfile, @Nullable Date date) {
        return getAllThirdPartiesForBrowserProfileAndDate(
                browserProfile,
                date,
                thirdPartyUrl -> {
                    try {
                        return Url.create(thirdPartyUrl.getDomain());
                    } catch (MalformedURLException e) {
                        return null;
                    }
                });

    }

    /**
     * Returns all unique third party hosts
     * @return A {@link Set} of the third-party {@link Url}s
     */
    public Set<Url> getAllThirdPartyHosts() {
        return getAllThirdPartyHostsForDate(null);
    }

    /**
     * Returns all unique third party hosts
     * @param date The {@link Date}. If null, all existing dates are examined
     * @return A {@link Set} of the third-party {@link Url}s
     */
    public Set<Url> getAllThirdPartyHostsForDate(@Nullable Date date) {
        return BrowserProfile.getAllBrowserProfiles().stream()
                .flatMap(browserProfile -> getAllThirdPartyHostsForBrowserProfileAndDate(browserProfile, date).stream())
                .collect(Collectors.toSet());
    }

    /**
     * Returns all unique third party hosts
     * @param browserProfile The {@link BrowserProfile}
     * @param date The {@link Date}
     * @return A {@link Set} with the {@link Url}s containing the domains
     */
    public Set<Url> getAllThirdPartyHostsForBrowserProfileAndDate(@NotNull BrowserProfile browserProfile, @Nullable Date date) {
        return getAllThirdPartiesForBrowserProfileAndDate(
                browserProfile,
                date,
                thirdPartyUrl -> {
                    try {
                        return Url.create(thirdPartyUrl.getHost());
                    } catch (MalformedURLException e) {
                        return null;
                    }
                });

    }

    /**
     * Returns a set of unique values of {@link Url}s of the third parties for a browser profile and a date
     * @param browserProfile The {@link BrowserProfile}
     * @param date The {@link Date}. If null, all existing dates are examined
     * @param converter The {@link Function} that indicates the mapping between the thirdPartyUrl as stored in the
     *                  database, and the format we want to bring it to
     * @return A {@link Set} of the third-party {@link Url}s
     */
    public Set<Url> getAllThirdPartiesForBrowserProfileAndDate(
            @NotNull BrowserProfile browserProfile,
            @Nullable Date date,
            @NotNull Function<Url, Url> converter) {

        // Set date constraint
        Query query = null;
        if (date != null) {
            String crawlDate = HttpRequestRecord.DATE_FORMAT.format(date);
            query = Query.query(Criteria.where("crawlDate").is(crawlDate));
        }

        return this.mongoTemplate
                .find(query, HttpRequestRecord.class, browserProfile.toCollectionName())
                .stream()
                .filter(HttpRequestRecord::isThirdPartyRequest)
                .map(HttpRequestRecord::getTargetUrl)
                .map(converter)
                .collect(Collectors.toSet());
    }

    /**
     * Clears all recrods for a specific {@link Date} and all {@link BrowserProfile}s
     * @param date The {@link Date}. If null, the whole collection is dropped
     */
    public void remove(@Nullable Date date) {
        BrowserProfile.getAllBrowserProfiles().stream()
                .forEach(browserProfile -> this.remove(browserProfile, date));
    }

    /**
     * Clears all records for a specific {@link BrowserProfile} and {@link Date}
     * @param browserProfile The {@link BrowserProfile}
     * @param date The {@link Date}. If null, the whole collection is dropped
     */
    public void remove(@NotNull BrowserProfile browserProfile, @Nullable Date date) {

        String collectionName = browserProfile.toCollectionName();
        if (date == null) {
            this.mongoTemplate.dropCollection(collectionName);
            LOGGER.info("Dropped collection " + collectionName);
        } else {
            String crawlDate = HttpRequestRecord.DATE_FORMAT.format(date);
            this.mongoTemplate.remove(
                    Query.query(Criteria.where("crawlDate").is(crawlDate)),
                    collectionName);
            LOGGER.info("Removed data for crawl date " + crawlDate + " for collection " + collectionName);
        }
    }

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
