package com.adblockers.entities;

import com.adblockers.entities.Url;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.MalformedURLException;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
@Document(collection = "firstPartyUrl")
public class FirstParty {
    private String url;
    @Transient private Url urlObject;
    private Integer rank;
    private String redirectionUrl;
    @Transient private Url redirectionUrlObject;

    public FirstParty() {}

    @PersistenceConstructor
    public FirstParty(Integer rank, String url) {
        setRank(rank);
        setUrl(url);
    }

    public Url getUrl() {
        return urlObject;
    }

    public void setUrl(String url) {
        try {
            this.urlObject = Url.create(url);
            this.url = url;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public Url getRedirectionUrl() {
        return redirectionUrlObject;
    }

    public void setRedirectionUrl(String redirectionUrl) {
        try {
            this.redirectionUrlObject = Url.create(redirectionUrl);
            this.redirectionUrl = redirectionUrl;
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
                .append(getUrl().getUrl())
                .append(", rank: ")
                .append(getRank())
                .append(", redirects to: ")
                .append(getRedirectionUrl().getUrl())
                .toString();
    }
}
