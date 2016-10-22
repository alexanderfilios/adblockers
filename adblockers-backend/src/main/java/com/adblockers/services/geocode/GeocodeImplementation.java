package com.adblockers.services.geocode;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.LegalEntityLocation;
import com.adblockers.entities.Location;
import com.adblockers.entities.Url;
import com.adblockers.repos.LegalEntityLocationRepository;
import com.adblockers.repos.LegalEntityRepository;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
@Component
public class GeocodeImplementation implements GeocodeService {

    private static final String BASE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/";
    private static final String ENCODING_FORMAT = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(GeocodeService.class);

    private LocationParser locationParser;
    private LegalEntityRepository legalEntityRepository;
    private LegalEntityLocationRepository legalEntityLocationRepository;

    public GeocodeImplementation() {}

    public Collection<LegalEntity> findLegalEntitiesWithoutGeocodeInformation() {
        // Find which legal entities (recognized by their domain) have geocode information
        // {google.com: true, unknowndomain.com: false}
        Map<Url, Boolean> hasLatLng = this.legalEntityLocationRepository.findAll().stream()
                .collect(Collectors.toMap(
                        legalEntityLocation -> legalEntityLocation.getUrl(),
                        legalEntityLocation -> legalEntityLocation.getLatitude() != null
                                && legalEntityLocation.getLongitude() != null,
                        (hasLatLng1, hasLatLng2) -> hasLatLng1 && hasLatLng2
                        ));

        // Filter out all legal entities for the domains of which we found latlng data
        return this.legalEntityRepository.findAll().stream()
                .filter(legalEntity -> !hasLatLng.getOrDefault(legalEntity.getUrl(), false))
                .collect(Collectors.toList());
    }

    public LegalEntityLocation findLocationByLegalEntity(LegalEntity legalEntity) {
        // Combine all data we have to extract an address for the legal entity

        // Input this address to the geocode api to get a location
        // Start with street, city, country
        Location location = findLocationByAddress(legalEntity.getFullAddress(0));
        // If not found, try with city, country
        if (location == null) {
            location = findLocationByAddress(legalEntity.getFullAddress(1));
        }
        // Finally with country
        if (location == null) {
            location = findLocationByAddress(legalEntity.getFullAddress(2));
        }
        if (location == null) {
            LOGGER.warn("Did not find LegalEntityLocation for " + legalEntity.getUrl().getDomain());
            return LegalEntityLocation.empty(legalEntity.getUrl());
        }

        LOGGER.info("LegalEntityLocation found for " + legalEntity.getUrl().getDomain());

        // Enhance the data with the legal entity information
        LegalEntityLocation legalEntityLocation = new LegalEntityLocation();
        legalEntityLocation.setLatitude(location.getLatitude());
        legalEntityLocation.setLongitude(location.getLongitude());
        legalEntityLocation.setCity(location.getCity());
        legalEntityLocation.setCountry(location.getCountry());
        legalEntityLocation.setPostalCode(location.getPostalCode());

        legalEntityLocation.setOrganization(legalEntity.getOrganization());
        legalEntityLocation.setDomain(legalEntity.getUrl().getDomain());

        return legalEntityLocation;
    }

    /**
     * Find the coordinates latlgn using any address data we have
     * @param address The address to lookup
     * @return A {@link Pair} with the latitude and longitude that we retrieved with the description in the mapping.
     * Returns null if the request failed.
     */
    private Pair<Double, Double> findLatLngByAddress(String address) {
        Map<String, String> addressComponents = ImmutableMap.<String, String>builder()
                .put("address", address)
                .build();
        URL url = getUrlForGetRequest(BASE_GEOCODE_URL, addressComponents);
        try (
                InputStream inputStream = url.openStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));) {
            return this.parseLatLng(new InputSource(bufferedReader));
            // If no data returned, the search has failed and we cannot lookup the location anymore
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Perform a reverse geocode lookup and find the city and country from the latlng data
     * @param latitude The latitude
     * @param longitude The longitude
     * @return A {@link Pair} containing the city and country. Null if something went wrong
     */
    private Map<String, String> findAdministrativeDataByLatLng(Double latitude, Double longitude) {
        Map<String, String> addressComponents = ImmutableMap.<String, String>builder()
                .put("latlng", latitude.toString() + "," + longitude.toString())
                .build();
        URL url = getUrlForGetRequest(BASE_GEOCODE_URL, addressComponents);
        try (
                InputStream inputStream = url.openStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                ) {
            return this.parseAdministrativeData(new InputSource(bufferedReader));
        } catch (IOException e) {
            return null;
        }
    }

    public Pair<Double, Double> parseLatLng(InputSource inputSource) {
        Map<String, String> params = ImmutableMap.<String, String>builder()
                .put("lat", "/GeocodeResponse/result/geometry/location/lat")
                .put("lng", "/GeocodeResponse/result/geometry/location/lng")
                .build();
        Map<String, String> results = locationParser.parseParams(inputSource, params);
        try {
            return Pair.of(
                    Double.parseDouble(results.getOrDefault("lat", null)),
                    Double.parseDouble(results.getOrDefault("lng", null))
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Map<String, String> parseAdministrativeData(InputSource inputSource) {
        Map<String, String> params = ImmutableMap.<String, String>builder()
                .put("administrativeAreaLevel1", "/GeocodeResponse/result[1]/address_component[type='administrative_area_level_1']/long_name")
                .put("administrativeAreaLevel2", "/GeocodeResponse/result[1]/address_component[type='administrative_area_level_2']/long_name")
                .put("postalCode", "/GeocodeResponse/result[1]/address_component[type='postal_code']/long_name")
                .put("country", "/GeocodeResponse/result[1]/address_component[type='country']/long_name")
                .build();
        Map<String, String> results = locationParser.parseParams(inputSource, params);

        return ImmutableMap.<String, String>builder()
                .put("city", results.containsKey("administrativeAreaLevel2")
                        ? results.get("administrativeAreaLevel2")
                        : results.getOrDefault("administrativeAreaLevel1", null))
                .put("postalCode", results.getOrDefault("postalCode", null))
                .put("country", results.getOrDefault("country", null))
                .build();
    }

    /**
     * Given an address string, it makes two geocode lookups to find the latlng and citycountry data, resp.
     * @param address The address data we have (can be an incomplete address, e.g. only city).
     * @return The {@link Location} corresponding to the address
     */
    private Location findLocationByAddress(String address) {

        if (StringUtils.isEmpty(address)) {
            return null;
        }

        // Geocode lookup
        Pair<Double, Double> latLng = findLatLngByAddress(address);
        if (latLng == null) {
            LOGGER.warn("No lat-lng data found for " + address);
            return null;
        }

        // Reverse geocode lookup
        Map<String, String> administrativeData = findAdministrativeDataByLatLng(latLng.getFirst(), latLng.getSecond());
        if (administrativeData == null) {
            LOGGER.warn("Lat-lng data found, but administrative data parsing failed for " + address);
        }

        // Both requests were successful
        Location location = new Location();
        location.setLatitude(latLng.getFirst());
        location.setLongitude(latLng.getSecond());

        location.setPostalCode(administrativeData.get("postalCode"));
        location.setCity(administrativeData.get("city"));
        location.setCountry(administrativeData.get("country"));

        return location;
    }

    /**
     * Formats and encodes the URL for a GET request
     * @param baseUrl The base URL for the service we are calling
     * @param params A mapping with the GET params
     * @return The {@link URL} with all the parameters encoded
     */
    private URL getUrlForGetRequest(String baseUrl, Map<String, String> params) {

        // Encode parameters
        Map<String, String> encodedParams = new HashMap<>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            try {
                encodedParams.put(
                        URLEncoder.encode(param.getKey(), ENCODING_FORMAT),
                        URLEncoder.encode(param.getValue(), ENCODING_FORMAT));
            } catch (UnsupportedEncodingException e) {}
        }

        // Build url with GET parameters
        String urlString = baseUrl
                + this.locationParser.getOutputFormat() + "?"
                + encodedParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        // Create URL object
        try {
            return new URL(urlString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Autowired
    public void setLocationParser(LocationParser locationParser) {
        this.locationParser = locationParser;
    }

    @Autowired
    public void setLegalEntityRepository(LegalEntityRepository legalEntityRepository) {
        this.legalEntityRepository = legalEntityRepository;
    }

    @Autowired
    public void setLegalEntityLocationRepository(LegalEntityLocationRepository legalEntityLocationRepository) {
        this.legalEntityLocationRepository = legalEntityLocationRepository;
    }
}
