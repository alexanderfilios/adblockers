package com.adblockers.httprequestrecord;

import com.adblockers.browserprofile.BrowserProfile;
import com.adblockers.utils.Url;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@Repository
public class HttpRequestRecordRepository {

    private MongoTemplate mongoTemplate;
    private static final Logger LOGGER = Logger.getLogger(HttpRequestRecordRepository.class);

    public void save(BrowserProfile browserProfile, HttpRequestRecord httpRequestRecord) {
        this.mongoTemplate.save(httpRequestRecord, browserProfile.toTableName());
    }
    public void save(BrowserProfile browserProfile, Collection<HttpRequestRecord> httpRequestRecords) {
        this.mongoTemplate.save(httpRequestRecords, browserProfile.toTableName());
    }
    public long count(BrowserProfile browserProfile) {
        return this.mongoTemplate.count(null, browserProfile.toTableName());
    }

    public List<?> getAllForBrowserProfile(BrowserProfile browserProfile) {
        return this.mongoTemplate.find(null, HttpRequestRecord.class, browserProfile.toTableName());
    }

    //TODO
    public List<Url> getAllThirdParties() {
        return new ArrayList<>();
    }


    public void remove(Date date) {
        BrowserProfile.getAllBrowserProfiles().stream()
                .forEach(browserProfile -> this.remove(browserProfile, date));
    }


    public void remove(BrowserProfile browserProfile, Date date) {
        if (browserProfile == null) {
            throw new IllegalArgumentException("Browser profile cannot be null. Call remove(Date) instead.");
        }
        String collectionName = browserProfile.toTableName();
        if (date == null) {
            this.mongoTemplate.dropCollection(collectionName);
            LOGGER.info("Dropped collection " + collectionName);
        } else {
            String crawlDate = HttpRequestRecord.dateFormat.format(date);
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
