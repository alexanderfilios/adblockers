package com.adblockers.entities;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.MalformedURLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
public class HttpRequestRecord {

    @Transient public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    @Field("source") private String sourceDomain;
    @Transient private Url sourceUrl;
    @Field("target") private String targetDomain;
    @Transient private Url targetUrl;
    private String crawlDate;
    @Transient private Date dateObject;
    private String contentType;

    @PersistenceConstructor
    public HttpRequestRecord(String sourceDomain, String targetDomain, String crawlDate, String contentType) {
        setSourceDomain(sourceDomain);
        setTargetDomain(targetDomain);

        if (crawlDate != null) {
            setCrawlDate(crawlDate);
        } else {
            setCrawlDate(new Date());
        }
        setContentType(contentType);
    }

    public HttpRequestRecord(String sourceDomain, String targetDomain, Long timestamp, String contentType) {
        setSourceDomain(sourceDomain);
        setTargetDomain(targetDomain);
        setCrawlDate(Date.from(new Timestamp(timestamp).toInstant()));
        setContentType(contentType);
    }

    public void setSourceDomain(String sourceDomain) {
        try {
            this.sourceUrl = Url.create(sourceDomain);
            this.sourceDomain = sourceDomain;
        } catch (MalformedURLException e) {
            // Leave the fields null
        }
    }
    public Url getSourceDomain() {
        return this.sourceUrl;
    }

    public void setTargetDomain(String targetDomain) {
        try {
            this.targetUrl = Url.create(targetDomain);
            this.targetDomain = targetDomain;
        } catch (MalformedURLException e) {
            // Leave the fields null
        }
    }
    public Url getTargetDomain() {
        return this.targetUrl;
    }

    public void setCrawlDate(String crawlDate) {
        try {
            this.dateObject = DATE_FORMAT.parse(crawlDate);
            this.crawlDate = crawlDate;
        } catch (ParseException e) {}
    }
    public void setCrawlDate(Date date) {
        this.crawlDate = DATE_FORMAT.format(date);
        this.dateObject = date;
    }
    public Date getCrawlDate() {
        return this.dateObject;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isThirdPartyRequest() {
        return sourceUrl != null && sourceUrl.hasSameDomainAs(targetUrl);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getSourceDomain())
                .append(" -> ")
                .append(getTargetDomain())
                .toString();
    }
}
