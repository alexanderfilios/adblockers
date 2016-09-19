package com.adblockers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
public class Url {

    private static final Pattern PATTERN = Pattern.compile("^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/?)([\\w\\-\\.]*[^#?\\s]*)(.*)?(#[\\w\\-]+)?$");

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
        this.domain = extractDomainFromHost(host);
        this.path = urlMatcher.group(4);
        this.file = urlMatcher.group(5) + urlMatcher.group(6);
        this.query = urlMatcher.group(7);
    }

    private String extractDomainFromHost(String host) {
        String[] components = host.split("\\.");

        if (components[components.length - 2].equalsIgnoreCase("co")) {
            return String.join(".", Arrays.copyOfRange(components, components.length - 3, components.length));
        } else {
            return String.join(".", Arrays.copyOfRange(components, components.length - 2, components.length));
        }
    }

    public String getStuffedHost() {
        return host.equals(domain)
                ? "www." + domain
                : host;
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
