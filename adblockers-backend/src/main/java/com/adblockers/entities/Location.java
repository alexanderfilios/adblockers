package com.adblockers.entities;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public class Location {
    private Double longitude;
    private Double latitude;
    private String city;
    private String country;
    private String postalCode;

    public Location() {}

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("city: ").append(getCity() != null ? getCity() : "-").append(", ")
                .append("postal code: ").append(getPostalCode() != null ? getPostalCode() : "-").append(", ")
                .append("country: ").append(getCountry() != null ? getCountry() : "-")
                .toString();
    }
}
