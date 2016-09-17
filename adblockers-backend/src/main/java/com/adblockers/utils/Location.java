package com.adblockers.utils;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
public abstract class Location {
    private Double longitude;
    private Double latitude;
    private String city;
    private String country;
    private Url url;

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

    public Url getUrl() {
        return url;
    }

    public void setUrl(Url url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("url: ").append(getUrl().getHost()).append(": ")
                .append("city: ").append(getCity()).append(", ")
                .append("country: ").append(getCountry())
                .toString();
    }
}
