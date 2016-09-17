package com.adblockers.utils;

/**
 * Created by alexandrosfilios on 17/09/16.
 * This class hosts data collected from GeoIP
 */
public class ServerLocation extends Location {

    @Override
    public String toString() {
        return "Server location: " + super.toString();
    }
}
