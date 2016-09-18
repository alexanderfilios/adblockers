package com.adblockers.services.geoip;

import com.adblockers.entities.ServerLocation;
import com.adblockers.entities.Url;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface GeoIpService {
    ServerLocation findServerLocationByUrl(Url url);

    default List<ServerLocation> findServerLocationsByUrl(List<Url> urls) {
        return urls.stream()
                .map(url -> findServerLocationByUrl(url))
                .collect(Collectors.toList());
    }
}
