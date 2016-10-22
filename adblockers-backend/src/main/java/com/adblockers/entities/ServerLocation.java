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

    public String getDomain() { return domain; }

    public Url getUrl() {
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

    public boolean isEmpty() {
        return getCountry() == null && getCity() == null;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Server (")
                .append(getUrl())
                .append(") location: ")
                .append(super.toString())
                .toString();
    }

    public static ServerLocation empty(Url url) {
        ServerLocation serverLocation = new ServerLocation();
        serverLocation.setDomain(url.getDomain());
        return serverLocation;
    }
}
