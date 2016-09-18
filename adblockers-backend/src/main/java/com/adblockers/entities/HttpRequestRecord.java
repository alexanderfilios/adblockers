package com.adblockers.entities;

import com.adblockers.entities.Url;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
public class HttpRequestRecord {

    @Transient public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");

    @Field("source") private String sourceUrl;
    @Transient private Url sourceUrlObject;
    @Field("target") private String targetUrl;
    @Transient private Url targetUrlObject;
    @Field("crawlDate") private String date;
    @Transient private Date dateObject;
    private String contentType;

    @PersistenceConstructor
    public HttpRequestRecord(String sourceUrl, String targetUrl, String contentType) {
        setSourceUrl(sourceUrl);
        setTargetUrl(targetUrl);
        setDate(new Date());
        setContentType(contentType);
    }

    public void setSourceUrl(String sourceUrl) {
        try {
            this.sourceUrlObject = Url.create(sourceUrl);
            this.sourceUrl = sourceUrl;
        } catch (MalformedURLException e) {
            // Leave the fields null
        }
    }
    public Url getSourceUrl() {
        return this.sourceUrlObject;
    }

    public void setTargetUrl(String targetUrl) {
        try {
            this.targetUrlObject = Url.create(targetUrl);
            this.targetUrl = targetUrl;
        } catch (MalformedURLException e) {
            // Leave the fields null
        }
    }
    public Url getTargetUrl() {
        return this.targetUrlObject;
    }

    public void setDate(String date) {
        try {
            this.dateObject = dateFormat.parse(date);
            this.date = date;
        } catch (ParseException e) {}
    }
    public void setDate(Date date) {
        this.date = dateFormat.format(date);
        this.dateObject = date;
    }
    public Date getDate() {
        return this.dateObject;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isThirdPartyRequest() {
        return sourceUrlObject != null && sourceUrlObject.hasSameDomainAs(targetUrlObject);
    }
}
