package com.adblockers.httprequestrecord;

import com.adblockers.browserprofile.BrowserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@Repository
public class HttpRequestRecordRepository {

    private MongoTemplate mongoTemplate;

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

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
