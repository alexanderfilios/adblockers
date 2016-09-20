package com.adblockers.entities;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.MalformedURLException;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@Document(collection = "firstPartyUrl")
public class FirstParty {
    @Field("url") private String domain;
    @Transient private Url url;
    private Integer rank;
    @Field("redirectionUrl") private String redirectionDomain;
    @Transient private Url redirectionUrl;

    public FirstParty() {}

    @PersistenceConstructor
    public FirstParty(Integer rank, String domain) {
        setRank(rank);
        setDomain(domain);
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

    public Url getRedirectionDomain() {
        return redirectionUrl;
    }

    public void setRedirectionDomain(String redirectionDomain) {
        try {
            this.redirectionUrl = Url.create(redirectionDomain);
            this.redirectionDomain = redirectionDomain;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("URL: ")
                .append(getDomain().getUrl())
                .append(", rank: ")
                .append(getRank())
                .append(", redirects to: ")
                .append(getRedirectionDomain().getUrl())
                .toString();
    }
}
