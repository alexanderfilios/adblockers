package com.adblockers.browserprofile;

import jdk.nashorn.internal.runtime.regexp.RegExp;
import org.apache.tomcat.util.codec.binary.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by alexandrosfilios on 15/09/16.
 */
public class BrowserProfile {

    public enum Adblocker {
        NOADBLOCKER,
        ADBLOCKPLUS,
        GHOSTERY,
        UNDEFINED
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

    public static BrowserProfile from(String browserProfileName) {
        String[] params = browserProfileName.replaceFirst("/^(?:data_)/", "").split("_");
        if (params.length < 2) {
            return new BrowserProfile(
                    Adblocker.UNDEFINED,
                    ProtectionLevel.DEFAULT,
                    UserAgent.DESKTOP
            );
        }

        Adblocker adblocker = Arrays.asList(Adblocker.values())
                .stream()
                .filter(a -> a.toString().toLowerCase().equals(params[0].toLowerCase()))
                .findFirst()
                .orElse(Adblocker.UNDEFINED);
        ProtectionLevel protectionLevel = params[1].equals("MaxProtection") || params[1].equals("MaxProtection")
                ? ProtectionLevel.MAX
                : ProtectionLevel.DEFAULT;
        UserAgent userAgent = params[params.length - 1].equals("MUA")
                ? UserAgent.MOBILE
                : UserAgent.DESKTOP;

        return new BrowserProfile(
                adblocker,
                protectionLevel,
                userAgent
        );
    }

    private BrowserProfile(Adblocker adblocker, ProtectionLevel protectionLevel, UserAgent userAgent) {
        this.adblocker = adblocker;
        this.protectionLevel = protectionLevel;
        this.userAgent = userAgent;
    }

    public String toTableName() {
        return new StringBuilder()
                .append("data_")
                .append(adblocker.toString().toLowerCase())
                .append("_")
                .append(protectionLevel.toString().toLowerCase())
                .append(UserAgent.DESKTOP.equals(userAgent) ? "" : "_mua")
                .toString();
    }

}
