package com.adblockers.services.geoip;

import com.adblockers.entities.ServerLocation;
import com.adblockers.entities.Url;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public interface GeoIpService {
    ServerLocation findServerLocationByUrl(@NotNull Url url);

    default Set<ServerLocation> findServerLocationsByUrl(@NotNull Set<Url> urls) {
        return urls.stream()
                .map(url -> findServerLocationByUrl(url))
                .collect(Collectors.toSet());
    }
}
