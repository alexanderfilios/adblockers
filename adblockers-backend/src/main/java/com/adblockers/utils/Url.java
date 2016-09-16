package com.adblockers.utils;

import java.net.MalformedURLException;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
public class Url {

    private String url;

    public static Url create(String url) throws MalformedURLException {
        return new Url(url);
    }

    private Url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Custom methods
    public boolean hasSameDomainAs(Url otherUrl) {
        return otherUrl != null && this.equals(otherUrl);
    }
}
