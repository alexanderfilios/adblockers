package com.adblockers.entities;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public class LegalEntityLocation extends Location {

    private String organization;

    public LegalEntityLocation() {
        super();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
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
