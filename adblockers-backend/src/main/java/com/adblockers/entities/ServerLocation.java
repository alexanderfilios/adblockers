package com.adblockers.entities;

import org.springframework.data.annotation.Transient;

import java.net.MalformedURLException;

/**
 * Created by alexandrosfilios on 17/09/16.
 * This class hosts data collected from GeoIP
 */
public class ServerLocation extends Location {

    private String domain;
    @Transient private Url url;

    public ServerLocation() {}

    public Url getDomain() {
        return url;
    }

    public void setDomain(String domain) {
        try {
            this.url = Url.create(domain);
            this.domain = domain;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Server (")
                .append(getDomain())
                .append(") location: ")
                .append(super.toString())
                .toString();
    }
}
