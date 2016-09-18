package com.adblockers.services.geocode;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.LegalEntityLocation;
import com.adblockers.entities.Location;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 17/09/16.
 */
@Component
public class GeocodeImplementation implements GeocodeService {

    private static final String BASE_GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/";
    private static final String ENCODING_FORMAT = "UTF-8";
    private LocationParser locationParser;

    public GeocodeImplementation() {}

    public LegalEntityLocation findLocationByLegalEntity(LegalEntity legalEntity) {
        // Combine all data we have to extract an address for the legal entity
        String address = Arrays.asList(new String[]{
                legalEntity.getAddress(),
                legalEntity.getCity(),
                legalEntity.getCountry()
        }).stream()
                .filter(addressElement -> addressElement != null)
                .collect(Collectors.joining(", "));


        // Input this address to the geocode api to get a location
        Location location = findLocationByAddress(address);
        if (location == null) {
            return null;
        }

        // Enhance the data with the legal entity information
        LegalEntityLocation legalEntityLocation = new LegalEntityLocation();
        legalEntityLocation.setLatitude(location.getLatitude());
        legalEntityLocation.setLongitude(location.getLongitude());
        legalEntityLocation.setCity(location.getCity());
        legalEntityLocation.setCountry(location.getCountry());
        legalEntityLocation.setOrganization(legalEntity.getOrganization());
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
            e.printStackTrace();
        }
        return null;
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
            e.printStackTrace();
        }
        return null;
    }

    public Pair<Double, Double> parseLatLng(InputSource inputSource) {
        Map<String, String> params = ImmutableMap.<String, String>builder()
                .put("lat", "/GeocodeResponse/result/geometry/location/lat")
                .put("lng", "/GeocodeResponse/result/geometry/location/lng")
                .build();
        Map<String, String> results = locationParser.parseParams(inputSource, params);
        Pair<Double, Double> latLng = Pair.of(
                Double.parseDouble(results.getOrDefault("lat", null)),
                Double.parseDouble(results.getOrDefault("lng", null))
        );
        return (latLng.getFirst() == null || latLng.getSecond() == null)
                ? null : latLng;
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
            return null;
        }

        // Reverse geocode lookup
        Map<String, String> administrativeData = findAdministrativeDataByLatLng(latLng.getFirst(), latLng.getSecond());

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
}