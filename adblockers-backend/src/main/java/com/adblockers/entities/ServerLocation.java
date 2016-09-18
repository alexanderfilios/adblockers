package com.adblockers.entities;

/**
 * Created by alexandrosfilios on 17/09/16.
 * This class hosts data collected from GeoIP
 */
public class ServerLocation extends Location {

    private String domain;

    public ServerLocation() {}

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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
