package com.adblockers.entities;

import org.springframework.data.annotation.Transient;

import java.net.MalformedURLException;

/**
 * Created by alexandrosfilios on 17/09/16.
 * This entity contains data from the {@link LegalEntity} objects
 * joined with geolocation information, if the {@link LegalEntity} object
 * contained any address (even incomplete).
 */
public class LegalEntityLocation extends Location {

    private String organization;
    private String domain;
    @Transient private Url url;

    public static LegalEntityLocation empty(Url url) {
        LegalEntityLocation legalEntityLocation = new LegalEntityLocation();
        legalEntityLocation.setDomain(url.getDomain());
        return legalEntityLocation;
    }

    public LegalEntityLocation() {
        super();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

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
        return getCountry() == null && getCity() == null
                && getLatitude() == null && getLongitude() == null;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Legal Entity (")
                .append(getOrganization())
                .append(") location: ")
                .append(super.toString())
                .toString();
    }
}
