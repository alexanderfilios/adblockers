package com.adblockers.entities;

import com.adblockers.utils.Utilities;
import org.apache.commons.collections.ListUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by alexandrosfilios on 15/09/16.
 */
public class BrowserProfile {

    public enum Adblocker {
        NOADBLOCKER,
        ADBLOCKPLUS,
        GHOSTERY
    }
    public enum ProtectionLevel {
        DEFAULT,
        MAX
    }
    public enum UserAgent {
        DESKTOP,
        MOBILE
    }

    private Adblocker adblocker;
    private ProtectionLevel protectionLevel;
    private UserAgent userAgent;

    public static BrowserProfile from(Adblocker adblocker, ProtectionLevel protectionLevel, UserAgent userAgent) {
        BrowserProfile browserProfile = new BrowserProfile();
        browserProfile.adblocker = adblocker;
        browserProfile.protectionLevel = protectionLevel;
        browserProfile.userAgent = userAgent;
        return  browserProfile;
    }

    /**
     * Inverse method to toCollectionName()
     * @param collectionName The collection name, e.g. data_noadblocker_max_mobile or noadblocker_max_mobile
     * @return The {@link BrowserProfile}
     */
    public static BrowserProfile from(String collectionName) {
        String[] params = collectionName.replaceFirst("^(data_)", "").split("_");
        if (params.length < 2) {
            throw new IllegalArgumentException("Invalid name");
        }

        Adblocker adblocker = Adblocker.valueOf(params[0].toUpperCase());
        ProtectionLevel protectionLevel = ProtectionLevel.valueOf(params[1].toUpperCase());
        UserAgent userAgent = UserAgent.valueOf(params[2].toUpperCase());

        return BrowserProfile.from(adblocker, protectionLevel, userAgent);
    }

    public static List<BrowserProfile> getAllBrowserProfiles() {
        // Start with 1-sized lists ([Adblocker], [Adblocker], ...)
        return Arrays.asList(BrowserProfile.Adblocker.values()).stream().map(t -> Arrays.asList(t.toString()))
                    // 2-sized lists ([Adblocker, ProtectionLevel], ...)
                    .flatMap(Utilities.crossWith(() -> Arrays.asList(BrowserProfile.ProtectionLevel.values()).stream().map(t -> Arrays.asList(t.toString())), (t1, t2) -> (List<String>) ListUtils.union(t1, t2)))
                    // 3-sized lists ([Adblocker, ProtectionLevel, UserAgent], ...)
                    .flatMap(Utilities.crossWith(() -> Arrays.asList(BrowserProfile.UserAgent.values()).stream().map(t -> Arrays.asList(t.toString())), (t1, t2) -> (List<String>) ListUtils.union(t1, t2)))
                    // Map each list to a BrowserProfile
                    .map(t -> BrowserProfile.from(Adblocker.valueOf(t.get(0)), ProtectionLevel.valueOf(t.get(1)), UserAgent.valueOf(t.get(2))))
                    // Collect them to a list
                    .collect(Collectors.toList());
    }

    public String toCollectionName() {
        return new StringBuilder()
                .append("data_")
                .append(toProfileName())
                .toString();
    }

    public String toProfileName() {
        return new StringBuilder()
                .append(adblocker.toString().toLowerCase())
                .append("_")
                .append(protectionLevel.toString().toLowerCase())
                .append("_")
                .append(userAgent.toString().toLowerCase())
                .toString();
    }

    public Adblocker getAdblocker() {
        return adblocker;
    }

    public ProtectionLevel getProtectionLevel() {
        return protectionLevel;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof BrowserProfile && ((BrowserProfile) other).toProfileName().equals(this.toProfileName());
    }

    @Override
    public int hashCode() {
        return toProfileName().hashCode();
    }

    @Override
    public String toString() {
        return toProfileName();
    }
}
