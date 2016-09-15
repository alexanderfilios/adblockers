package com.adblockers.browserprofile;

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

    public static BrowserProfile from(String adblocker, Boolean defaultProtection, Boolean mobileUserAgent) {
        return new BrowserProfile(
                Adblocker.valueOf(adblocker),
                defaultProtection ? ProtectionLevel.DEFAULT : ProtectionLevel.MAX,
                mobileUserAgent ? UserAgent.MOBILE : UserAgent.DESKTOP);
    }
    public static BrowserProfile from(Adblocker adblocker, ProtectionLevel protectionLevel, UserAgent userAgent) {
        return new BrowserProfile(adblocker, protectionLevel, userAgent);
    }

    private BrowserProfile(Adblocker adblocker, ProtectionLevel protectionLevel, UserAgent userAgent) {
        this.adblocker = adblocker;
        this.protectionLevel = protectionLevel;
        this.userAgent = userAgent;
    }

    public String toTableName() {
        return new StringBuilder()
                .append(adblocker.toString().toLowerCase())
                .append("_")
                .append(protectionLevel.toString().toLowerCase())
                .append(UserAgent.DESKTOP.equals(userAgent) ? "" : "_mua")
                .toString();
    }

}
