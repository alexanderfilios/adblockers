package com.adblockers.entities;

import org.springframework.data.annotation.Transient;

import java.net.MalformedURLException;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public class LegalEntityLocation extends Location {

    private String organization;
    private String domain;
    @Transient private Url url;

    public LegalEntityLocation() {
        super();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

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
                .append("Legal Entity (")
                .append(getOrganization())
                .append(") location: ")
                .append(super.toString())
                .toString();
    }
}
