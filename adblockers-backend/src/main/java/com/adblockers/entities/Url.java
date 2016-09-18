package com.adblockers.entities;

import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
public class Url {

    private String url;

    public static Url create(String url) throws MalformedURLException {
        return new Url(url);
    }

    private Url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Custom methods
    public boolean hasSameDomainAs(Url otherUrl) {
        return otherUrl != null && this.equals(otherUrl);
    }

    /**
     * Finds the host, e.g. http://www.google.com/some/path -> www.google.com
     * @return The host
     */
    public String getHost() {
        return getUrl()
                // Remove the protocol
                .replaceFirst("^[a-zA-Z]*\\://", "")
                // Remove all paths
                .replaceFirst("/.*", "");
    }

    public String getDomain() {
        // Split domains
        String[] components = getHost().split("\\.");

        if (components.length < 2
                || (components[components.length - 2].equalsIgnoreCase("co") && components.length < 3)) {

            return null;
        }
        if (components[components.length - 2].equalsIgnoreCase("co")) {
            return String.join(".", Arrays.copyOfRange(components, components.length - 3, components.length));
        } else {
            return String.join(".", Arrays.copyOfRange(components, components.length - 2, components.length));
        }
    }

    @Override
    public String toString() {
        return getUrl();
    }
}