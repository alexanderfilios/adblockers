package com.adblockers.services.geoip;

import com.adblockers.AdblockersBackendApplication;
import com.adblockers.entities.ServerLocation;
import com.adblockers.entities.Url;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@Component
public class GeoIpCity implements GeoIpService {

    private static final String GEOIP_DATABASE = "GeoLite2-City.mmdb";
    private static final Logger LOGGER = Logger.getLogger(GeoIpService.class);

    private DatabaseReader databaseReader;


    public GeoIpCity() {
        try {
            File database = new File(AdblockersBackendApplication.RESOURCES_PATH + GEOIP_DATABASE);
            databaseReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerLocation findServerLocationByUrl(Url url) {
        try {
            InetAddress ipAddress = InetAddress.getByName(url.getHost());
            CityResponse cityResponse = databaseReader.city(ipAddress);

            LOGGER.info("ServerLocation found for " + url.getDomain());

            ServerLocation serverLocation = new ServerLocation();
            serverLocation.setPostalCode(cityResponse.getPostal().getCode());
            serverLocation.setCity(cityResponse.getCity().getName());
            serverLocation.setCountry(cityResponse.getCountry().getName());
            serverLocation.setLatitude(cityResponse.getLocation().getLatitude());
            serverLocation.setLongitude(cityResponse.getLocation().getLongitude());

            serverLocation.setDomain(url.getDomain());
            return serverLocation;
        } catch (IOException e) {
            LOGGER.warn("Did not find host " + url.getHost());
        } catch (GeoIp2Exception e) {
            LOGGER.warn("GeoIp exception thrown");
        }
        return null;
    }
}
