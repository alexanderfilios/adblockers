package com.adblockers.services.geoip;

import com.adblockers.entities.ServerLocation;
import com.adblockers.entities.Url;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface GeoIpService {
    ServerLocation getServerLocationByUrl(Url url);

    default List<ServerLocation> getServerLocationsByUrl(List<Url> urls) {
        return urls.stream()
                .map(url -> getServerLocationByUrl(url))
                .collect(Collectors.toList());
    }
}
