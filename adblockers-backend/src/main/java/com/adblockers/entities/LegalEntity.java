package com.adblockers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 16/09/16.
 * This class hosts data collected from WHOIS
 */
public class LegalEntity {

    /**
     * Contains a matching of the properties of the class and a list of possible names where this attribute may be stored in a WHOIS response
     * Property keys are lower case
     */
    public static final Map<String, List<String>> PROP_NAMES = ImmutableMap.<String, List<String>>builder()
            .put("entityDomain", ImmutableList.of("entityDomain", "entityDomain name", "domain name", "domain"))
            .put("organization", ImmutableList.of("organization", "organisation", "tech organization", "admin organization", "tech organisation", "admin organisation", "org", "registrant organisation", "registrant organization"))
            .put("email", ImmutableList.of("email", "admin email", "tech email", "registrant email"))
            .put("city", ImmutableList.of("city", "admin city", "tech city", "registrant city"))
            .put("address", ImmutableList.of("address", "street", "admin address", "admin street", "tech address", "tech street"))
            .put("country", ImmutableList.of("country", "countrycode", "admin country", "tech country", "registrant country"))
            .build();

    @Id
    private String id;

    // This is the domain we visited initially
    private String domain;
    @Transient private Url url;

    // Data from the WHOIS information
    private String entityDomain;
    @Transient private Url entityUrl;
    private String organization;
    private String email;
    private String city;
    private String address;
    private String country;

    public LegalEntity() {}

    public static LegalEntity empty(Url url) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setDomain(url.getDomain());
        return legalEntity;
    }

    @PersistenceConstructor
    public LegalEntity(String id, String domain, String entityDomain, String organization, String email, String city, String address, String country) {
        setId(id);
        if (!StringUtils.isEmpty(domain)) {
            setDomain(domain);
        }
        if (!StringUtils.isEmpty(entityDomain)) {
            setEntityDomain(entityDomain);
        }
        setOrganization(organization);
        setEmail(email);
        setCity(city);
        setAddress(address);
        setCountry(country);
    }

    public String getEntityDomain() { return entityDomain; }

    public Url getEntityUrl() {
        return entityUrl;
    }

    public void setEntityDomain(String entityDomain) {
        try {
            this.entityUrl = Url.create(entityDomain);
            this.entityDomain = entityDomain;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @JsonIgnore
    public String getDisplayCountry() {
        return !StringUtils.isEmpty(getCountry())
                ? new Locale("", getCountry()).getDisplayCountry()
                : null;
    }

    @JsonIgnore
    public String getFullAddress(Integer skip) {
        return Arrays.asList(new String[]{
                getAddress(),
                getCity(),
                getDisplayCountry()
        }).stream()
                .skip(skip)
                .filter(addressElement -> addressElement != null)
                .map(addressElement -> addressElement.replaceFirst("(^[\\s,]+)", ""))
                .map(addressElement -> addressElement.replaceFirst("([\\s,]+$)", ""))
                .collect(Collectors.joining(", "));
    }

    public static LegalEntity fromPropertiesMap(String domain, Map<String, String> props) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setDomain(domain);
        try {
            legalEntity.setEntityDomain(Url.create(getPropertyOrNull(props, "entityDomain")).getDomain());
        } catch (MalformedURLException | NullPointerException e) {}
        legalEntity.setOrganization(getPropertyOrNull(props, "organization"));
        legalEntity.setEmail(getPropertyOrNull(props, "email"));
        legalEntity.setAddress(getPropertyOrNull(props, "address"));
        legalEntity.setCity(getPropertyOrNull(props, "city"));
        legalEntity.setCountry(getPropertyOrNull(props, "country"));

        return legalEntity;
    }

    private static String getPropertyOrNull(Map<String, String> props, String propName) {
        return PROP_NAMES.get(propName).stream()
                // Supose propName is organization
                // candidateName will be each time "organization", "organisation", etc.
                .map(candidateName -> props.keySet().stream()
                        // mapPropName is the key of the mapping.
                        // Suppose we have the mapping {Organisation: Google Inc.}
                        .filter(mapPropName -> mapPropName.trim().equalsIgnoreCase(candidateName))
                        // For the mapPropNames that matched, map them to the value in the map
                        .map(mapPropName -> props.get(mapPropName))
                        // Only get the first (lazy evaluation)
                        .findFirst()
                        // If none is found, return null
                        .orElse(null))
                // If none is found, return null
                .filter(propertyFound -> propertyFound != null)
                .findFirst()
                .orElse(null);
    }

    public String toString() {
        return ImmutableMap.<String, String>builder()
                .put("entityDomain", this.getEntityUrl() != null && this.getEntityUrl().getDomain() != null ? this.getEntityUrl().getDomain() : "")
                .put("domain", this.getUrl() != null && this.getUrl().getDomain() != null ? this.getUrl().getDomain() : "")
                .put("organization", this.getOrganization() != null ? this.getOrganization() : "")
                .put("email", this.getEmail() != null ? this.getEmail() : "")
                .put("address", this.getAddress() != null ? this.getAddress() : "")
                .put("city", this.getCity() != null ? this.getCity() : "")
                .put("country", this.getCountry() != null ? this.getCountry() : "")
                .build().toString();
    }
}
