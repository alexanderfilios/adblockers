package com.adblockers.entities;

import javafx.util.Pair;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.MalformedURLException;

/**
 * Created by alexandrosfilios on 20/10/16.
 * Class used for conversion of legacy data
 */
@Document(collection = "entity_details")
public class LegalEntityLocationAdapter {
    private String domain;
    @Field("admin_org") private String adminOrganization;
    @Field("admin_country") private String adminCountry;
    @Field("admin_city") private String adminCity;
    @Field("admin_street") private String adminStreet;

    @Field("regis_org") private String registryOrganization;
    @Field("regis_country") private String registryCountry;
    @Field("regis_city") private String registryCity;
    @Field("regis_street") private String registryStreet;

    @Field("tech_org") private String technicalOrganization;
    @Field("tech_country") private String technicalCountry;
    @Field("tech_city") private String technicalCity;
    @Field("tech_street") private String technicalStreet;

    private LocationAdapter location;

    private String getOrganization() {
        String organization = getAdminOrganization() != null ? getAdminOrganization()
                : getRegistryOrganization() != null ? getRegistryOrganization()
                : getTechnicalOrganization() != null ? getTechnicalOrganization()
                : "";
        return organization.replace("_", " ");
    }
    private String getCountry() {
        String country = getAdminCountry() != null ? getAdminCountry()
                : getRegistryCountry() != null ? getRegistryCountry()
                : getTechnicalCountry() != null ? getTechnicalCountry()
                : "";
        return country.replace("_", " ");
    }
    private String getCity() {
        String city = getAdminCity() != null ? getAdminCity()
                : getRegistryCity() != null ? getRegistryCity()
                : getTechnicalCity() != null ? getTechnicalCity()
                : "";
        return city.replace("_", " ");
    }
    private String getStreet() {
        String street = getAdminStreet() != null ? getAdminStreet()
                : getRegistryStreet() != null ? getRegistryStreet()
                : getTechnicalStreet() != null ? getTechnicalStreet()
                : "";
        return street.replace("_", " ");
    }

    public boolean isServerLocationEmpty() {
        return getLocation() == null || getLocation().getLat() == null && getLocation().getLng() == null;
    }
    public boolean isLegalEntityLocationEmpty() {
        return getCountry() == null && getCity() == null && getStreet() == null;
    }

    public Url getDomainUrl() {
        try {
            return Url.create(getDomain());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public Pair<LegalEntityLocation, ServerLocation> toLocations() {
        LegalEntityLocation legalEntityLocation = new LegalEntityLocation();
        legalEntityLocation.setDomain(getDomain());
        legalEntityLocation.setOrganization(getOrganization());
        legalEntityLocation.setCountry(getCountry());
        legalEntityLocation.setCity(getCity());

        ServerLocation serverLocation = new ServerLocation();
        serverLocation.setDomain(getDomain());
        if (getLocation() != null) {
            serverLocation.setCountry(getLocation().getCountry());
            try {
                serverLocation.setLatitude(Double.parseDouble(getLocation().getLat()));
                serverLocation.setLongitude(Double.parseDouble(getLocation().getLng()));
            } catch (NumberFormatException e) {}
        }

        return new Pair<>(legalEntityLocation, serverLocation);
    }

    public class LocationAdapter {
        private String country;
        private String lat;
        private String lng;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAdminOrganization() {
        return adminOrganization;
    }

    public void setAdminOrganization(String adminOrganization) {
        this.adminOrganization = adminOrganization;
    }

    public String getAdminCountry() {
        return adminCountry;
    }

    public void setAdminCountry(String adminCountry) {
        this.adminCountry = adminCountry;
    }

    public String getAdminCity() {
        return adminCity;
    }

    public void setAdminCity(String adminCity) {
        this.adminCity = adminCity;
    }

    public String getAdminStreet() {
        return adminStreet;
    }

    public void setAdminStreet(String adminStreet) {
        this.adminStreet = adminStreet;
    }

    public String getRegistryOrganization() {
        return registryOrganization;
    }

    public void setRegistryOrganization(String registryOrganization) {
        this.registryOrganization = registryOrganization;
    }

    public String getRegistryCountry() {
        return registryCountry;
    }

    public void setRegistryCountry(String registryCountry) {
        this.registryCountry = registryCountry;
    }

    public String getRegistryCity() {
        return registryCity;
    }

    public void setRegistryCity(String registryCity) {
        this.registryCity = registryCity;
    }

    public String getRegistryStreet() {
        return registryStreet;
    }

    public void setRegistryStreet(String registryStreet) {
        this.registryStreet = registryStreet;
    }

    public String getTechnicalOrganization() {
        return technicalOrganization;
    }

    public void setTechnicalOrganization(String technicalOrganization) {
        this.technicalOrganization = technicalOrganization;
    }

    public String getTechnicalCountry() {
        return technicalCountry;
    }

    public void setTechnicalCountry(String technicalCountry) {
        this.technicalCountry = technicalCountry;
    }

    public String getTechnicalCity() {
        return technicalCity;
    }

    public void setTechnicalCity(String technicalCity) {
        this.technicalCity = technicalCity;
    }

    public String getTechnicalStreet() {
        return technicalStreet;
    }

    public void setTechnicalStreet(String technicalStreet) {
        this.technicalStreet = technicalStreet;
    }

    public LocationAdapter getLocation() {
        return location;
    }

    public void setLocation(LocationAdapter location) {
        this.location = location;
    }
}
