package com.adblockers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
public class Url {

    private static final Pattern PATTERN = Pattern.compile("^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/?)([\\w\\-\\.]*[^#?\\s]*)(.*)?(#[\\w\\-]+)?$");
    private static final List<String> COMMON_CCSLDS = ImmutableList.<String>builder()
            .add("co")
            .add("com")
            .add("ac")
            .build();

    private String url;
    @JsonIgnore private String protocol;
    @JsonIgnore private String host;
    @JsonIgnore private String domain;
    @JsonIgnore private String path;
    @JsonIgnore private String file;
    @JsonIgnore private String query;

    public static Url create(String url) throws MalformedURLException {
        return new Url(url);
    }

    private Url(String url) throws MalformedURLException {
        Matcher urlMatcher = PATTERN.matcher(url);
        if (!urlMatcher.matches()) {
            throw new MalformedURLException();
        }
        this.url = urlMatcher.group(0);
        this.protocol = urlMatcher.group(2);
        this.host = urlMatcher.group(3);
        this.domain = extractDomain();
        this.path = urlMatcher.group(4);
        this.file = urlMatcher.group(5) + urlMatcher.group(6);
        this.query = urlMatcher.group(7);
    }

    private String extractDomain() {
        String[] components = getHost().split("\\.");

        if (hasCCSLD()) {
            return String.join(".", Arrays.copyOfRange(components, components.length - 3, components.length));
        } else if (components.length > 1) {
            return String.join(".", Arrays.copyOfRange(components, components.length - 2, components.length));
        } else {
            return null;
        }
    }

    /**
     * Catch cases of countries with domains like co.uk, com.au, ac.uk
     * @return
     */
    private boolean hasCCSLD() {
        String[] components = getHost().split("\\.");
        return components.length > 2
                && COMMON_CCSLDS.contains(components[components.length - 2].toLowerCase());
    }

    public String getStuffedHost() {
        return getHost().equals(getDomain())
                ? "www." + getDomain()
                : getHost();
    }
    public String getStuffedUrl() {
        return getProtocol() == null
                ? "http://" + getStuffedHost()
                : getUrl();
    }

    public String getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    // Custom methods
    public boolean hasSameDomainAs(Url otherUrl) {
        return otherUrl != null && getDomain().equals(otherUrl.getDomain());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Url
                && ((Url) other).getUrl().equals(this.getUrl());
    }

    @Override
    public int hashCode() {
        return this.getUrl().hashCode();
    }

    @Override
    public String toString() {
        return getUrl();
    }

    public String toDetailedString() {
        return new StringBuilder()
                .append("Url: ").append(getUrl()).append(", ")
                .append("Protocol: ").append(getProtocol()).append(", ")
                .append("Host: ").append(getStuffedHost()).append(", ")
                .append("Domain: ").append(getDomain()).append(", ")
                .append("File: ").append(getFile()).append(", ")
                .append("Query: ").append(getQuery())
                .toString();

    }
}
