package com.adblockers;

import com.adblockers.entities.LegalEntity;
import com.adblockers.entities.LegalEntityLocation;
import com.adblockers.entities.ServerLocation;
import com.adblockers.entities.Url;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.geoip.GeoIpService;
import com.adblockers.services.whois.WhoisService;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AdblockersBackendApplicationTests {

	private WhoisService whoisService;
    private GeoIpService geoIpService;
    private GeocodeService geocodeService;

    private List<Url> testUrls;

    public AdblockersBackendApplicationTests() {
        try {
            this.testUrls = Arrays.asList(new Url[]{
                    Url.create("www.google.com"),
                    Url.create("www.facebook.com")
            });
        } catch(MalformedURLException e) {
            this.testUrls = null;
        }
    }

    @Test
    public void findLegalEntityByUrlTest() {
        List<LegalEntity> testResults = this.whoisService
                .findLegalEntitiesByUrl(this.testUrls);

        assertThat("google.com returns correct results", testResults.get(0),
                allOf(
                        hasProperty("domain", is("google.com")),
                        hasProperty("url", hasProperty("url", is("google.com"))),
                        hasProperty("organization", is("Google Inc.")),
                        hasProperty("email", is("dns-admin@google.com")),
                        hasProperty("address", is("1600 Amphitheatre Parkway")),
                        hasProperty("city", is("Mountain View")),
                        hasProperty("country", is("US"))
                ));
        assertThat("facebook.com returns correct results", testResults.get(1),
                allOf(
                        hasProperty("domain", is("facebook.com")),
                        hasProperty("url", hasProperty("url", is("facebook.com"))),
                        hasProperty("organization", is("Facebook, Inc.")),
                        hasProperty("email", is("domain@fb.com")),
                        hasProperty("address", is("1601 Willow Road,")),
                        hasProperty("city", is("Menlo Park")),
                        hasProperty("country", is("US"))
                ));
    }

    @Test
    public void findServerLocationByUrlTest() {
        List<ServerLocation> testResults = this.geoIpService
                .findServerLocationsByUrl(this.testUrls);

        assertThat("google.com returns correct results", testResults.get(0),
                allOf(
                        hasProperty("domain", is("google.com")),
                        hasProperty("latitude", is(Double.valueOf(37.419200000000004))),
                        hasProperty("longitude", is(Double.valueOf(-122.0574))),
                        hasProperty("postalCode", is("94043")),
                        hasProperty("city", is("Mountain View")),
                        hasProperty("country", is("United States"))
                ));
        assertThat("facebook.com returns correct results", testResults.get(1),
                allOf(
                        hasProperty("domain", is("facebook.com")),
                        hasProperty("latitude", is(Double.valueOf(53.3478))),
                        hasProperty("longitude", is(Double.valueOf(-6.2597))),
                        hasProperty("postalCode", is(nullValue())),
                        hasProperty("city", is(nullValue())),
                        hasProperty("country", is("Ireland"))
                ));

    }

    @Test
    public void findLocationByLegalEntityTest() {
        List<LegalEntity> testLegalEntities = this.testUrls
                .stream()
                .map(testUrl -> this.whoisService.findLegalEntityByUrl(testUrl))
                .collect(Collectors.toList());
        List<LegalEntityLocation> testResults = this.geocodeService
                .findLocationsByLegalEntity(testLegalEntities);

        assertThat("google.com returns correct results", testResults.get(0),
                allOf(
                        hasProperty("organization", is("Google Inc.")),
                        hasProperty("latitude", is(Double.valueOf(37.4224484))),
                        hasProperty("longitude", is(Double.valueOf(-122.0843249))),
                        hasProperty("postalCode", is("94043")),
                        hasProperty("city", is("Santa Clara County")),
                        hasProperty("country", is("United States"))
                ));
        assertThat("facebook.com returns correct results", testResults.get(1),
                allOf(
                        hasProperty("organization", is("Facebook, Inc.")),
                        hasProperty("latitude", is(Double.valueOf(37.4851021))),
                        hasProperty("longitude", is(Double.valueOf(-122.1474466))),
                        hasProperty("postalCode", is("94025")),
                        hasProperty("city", is("San Mateo County")),
                        hasProperty("country", is("United States"))
                ));
    }



    @Autowired
    public void setWhoisService(WhoisService whoisService) {
        this.whoisService = whoisService;
    }

    @Autowired
    public void setGeoIpService(GeoIpService geoIpService) {
        this.geoIpService = geoIpService;
    }

    @Autowired
    public void setGeocodeService(GeocodeService geocodeService) {
        this.geocodeService = geocodeService;
    }

}
