package com.adblockers.scripts;

import com.adblockers.AdblockersBackendApplication;
import com.adblockers.utils.ServerLocation;
import com.adblockers.utils.Url;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by alexandrosfilios on 16/09/16.
 */
@Component
public class GeoIpService {

    private static final String GEOIP_DATABASE = "GeoLite2-City.mmdb";
    private DatabaseReader databaseReader;


    public GeoIpService() {
        try {
            File database = new File(AdblockersBackendApplication.RESOURCES_PATH + GEOIP_DATABASE);
            databaseReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ServerLocation> getServerLocationsByUrl(List<Url> urls) {
        List<ServerLocation> serverLocations = new LinkedList<>();
        for (Url url : urls) {
            try {
                serverLocations.add(this.getServerLocationByUrl(url));
            } catch (IOException | GeoIp2Exception e) {
                e.printStackTrace();
            }
        }
        return serverLocations;
    }

    public ServerLocation getServerLocationByUrl(Url url) throws UnknownHostException, IOException, GeoIp2Exception {
        InetAddress ipAddress = InetAddress.getByName(url.getHost());
        CityResponse cityResponse = databaseReader.city(ipAddress);

        ServerLocation serverLocation = new ServerLocation();
        serverLocation.setUrl(url);
        serverLocation.setCity(cityResponse.getCity().getName());
        serverLocation.setCountry(cityResponse.getCountry().getName());
        serverLocation.setLatitude(cityResponse.getLocation().getLatitude());
        serverLocation.setLongitude(cityResponse.getLocation().getLongitude());
        return serverLocation;
    }
}
