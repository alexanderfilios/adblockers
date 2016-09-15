package com.adblockers.httprequestrecord;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
public class HttpRequestRecord {

    SimpleDateFormat dateFormat = new SimpleDateFormat("DD-MM-YYYY");

    @Field("source")
    private String sourceUrl;
    @Transient
    private URL sourceUrlObject;
    @Field("target")
    private String targetUrl;
    @Transient
    private URL targetUrlObject;
    private String date;
    @Transient
    private Date dateObject;

    public HttpRequestRecord(String sourceUrl, String targetUrl) {
        setSourceUrl(sourceUrl);
        setTargetUrl(targetUrl);
        setDate(new Date());
    }

    public void setSourceUrl(String sourceUrl) {
        try {
            this.sourceUrlObject = new URL(sourceUrl);
            this.sourceUrl = sourceUrl;
        } catch(MalformedURLException e) {}
    }
    public URL getSourceUrl() {
        return this.sourceUrlObject;
    }

    public void setTargetUrl(String targetUrl) {
        try {
            this.targetUrlObject = new URL(targetUrl);
            this.targetUrl = targetUrl;
        } catch(MalformedURLException e) {}
    }
    public URL getTargetUrl() {
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
}
