package com.adblockers.browserprofile;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
public class BrowserProfileDTO {
    private String adblocker;
    private Boolean defaultProtection;
    private Boolean mobileUserAgent;

    public BrowserProfile toBrowserProfile() {
        return BrowserProfile.from(adblocker, defaultProtection, mobileUserAgent);
    }

    public BrowserProfileDTO() {}

    public String getAdblocker() {
        return adblocker;
    }

    public void setAdblocker(String adblocker) {
        this.adblocker = adblocker;
    }

    public Boolean getDefaultProtection() {
        return defaultProtection;
    }

    public void setDefaultProtection(Boolean defaultProtection) {
        this.defaultProtection = defaultProtection;
    }

    public Boolean getMobileUserAgent() {
        return mobileUserAgent;
    }

    public void setMobileUserAgent(Boolean mobileUserAgent) {
        this.mobileUserAgent = mobileUserAgent;
    }
}
