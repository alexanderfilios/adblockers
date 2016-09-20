package com.adblockers;

import com.adblockers.entities.*;
import com.adblockers.services.geocode.GeocodeService;
import com.adblockers.services.geoip.GeoIpService;
import com.adblockers.services.requestgraph.RequestGraph;
import com.adblockers.services.whois.WhoisService;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AdblockersBackendApplicationTests {

	private WhoisService whoisService;
    private GeoIpService geoIpService;
    private GeocodeService geocodeService;

    private Set<Url> testUrls;

    public AdblockersBackendApplicationTests() {
        try {
            this.testUrls = ImmutableSet.<Url>builder()
                    .add(Url.create("www.facebook.com"))
                    .add(Url.create("www.google.com"))
                    .build();
        } catch(MalformedURLException e) {
            this.testUrls = null;
        }
    }

    @Test
    public void findLegalEntityByUrlTest() {
        LegalEntity[] testResults = this.whoisService
                .findLegalEntitiesByUrl(this.testUrls)
                .stream()
                .sorted((r1, r2) -> r1.getUrl().getDomain().compareTo(r2.getUrl().getDomain()))
                .toArray(size -> new LegalEntity[size]);

        assertThat("facebook.com returns correct results", testResults[0],
                allOf(
                        hasProperty("domain", hasProperty("domain", is("facebook.com"))),
                        hasProperty("entityDomain", hasProperty("domain", is("facebook.com"))),
                        hasProperty("organization", is("Facebook, Inc.")),
                        hasProperty("email", is("domain@fb.com")),
                        hasProperty("address", is("1601 Willow Road,")),
                        hasProperty("city", is("Menlo Park")),
                        hasProperty("country", is("US"))
                ));
        assertThat("google.com returns correct results", testResults[1],
                allOf(
                        hasProperty("domain", hasProperty("domain", is("google.com"))),
                        hasProperty("entityDomain", hasProperty("domain", is("google.com"))),
                        hasProperty("organization", is("Google Inc.")),
                        hasProperty("email", is("dns-admin@google.com")),
                        hasProperty("address", is("1600 Amphitheatre Parkway")),
                        hasProperty("city", is("Mountain View")),
                        hasProperty("country", is("US"))
                ));
    }

    @Test
    public void findServerLocationByUrlTest() {
        ServerLocation[] testResults = this.geoIpService
                .findServerLocationsByUrl(this.testUrls)
                .stream()
                .sorted((r1, r2) -> r1.getDomain().getDomain().compareTo(r2.getDomain().getDomain()))
                .toArray(size -> new ServerLocation[size]);

        assertThat("facebook.com returns correct results", testResults[0],
                allOf(
                        hasProperty("domain", hasProperty("domain", is("facebook.com"))),
                        hasProperty("latitude", is(Double.valueOf(53.3478))),
                        hasProperty("longitude", is(Double.valueOf(-6.2597))),
                        hasProperty("postalCode", is(nullValue())),
                        hasProperty("city", is(nullValue())),
                        hasProperty("country", is("Ireland"))
                ));
        assertThat("google.com returns correct results", testResults[1],
                allOf(
                        hasProperty("domain", hasProperty("domain", is("google.com"))),
                        hasProperty("latitude", is(Double.valueOf(37.419200000000004))),
                        hasProperty("longitude", is(Double.valueOf(-122.0574))),
                        hasProperty("postalCode", is("94043")),
                        hasProperty("city", is("Mountain View")),
                        hasProperty("country", is("United States"))
                ));
    }

    @Test
    public void findLocationByLegalEntityTest() {
        Set<LegalEntity> testLegalEntities = this.testUrls
                .stream()
                .map(testUrl -> this.whoisService.findLegalEntityByUrl(testUrl))
                .collect(Collectors.toSet());

        LegalEntityLocation[] testResults = this.geocodeService
                .findLocationsByLegalEntity(testLegalEntities)
                .stream()
                .sorted((r1, r2) -> r1.getDomain().getDomain().compareTo(r2.getDomain().getDomain()))
                .toArray(size -> new LegalEntityLocation[size]);

        assertThat("facebook.com returns correct results", testResults[0],
                allOf(
                        hasProperty("domain", hasProperty("domain", is("facebook.com"))),
                        hasProperty("organization", is("Facebook, Inc.")),
                        hasProperty("latitude", is(Double.valueOf(37.4851021))),
                        hasProperty("longitude", is(Double.valueOf(-122.1474466))),
                        hasProperty("postalCode", is("94025")),
                        hasProperty("city", is("San Mateo County")),
                        hasProperty("country", is("United States"))
                ));
        assertThat("google.com returns correct results", testResults[1],
                allOf(
                        hasProperty("domain", hasProperty("domain", is("google.com"))),
                        hasProperty("organization", is("Google Inc.")),
                        hasProperty("latitude", is(Double.valueOf(37.4224484))),
                        hasProperty("longitude", is(Double.valueOf(-122.0843249))),
                        hasProperty("postalCode", is("94043")),
                        hasProperty("city", is("Santa Clara County")),
                        hasProperty("country", is("United States"))
                ));
    }

    @Test
    public void createGraphTest() {
        Set<Pair<String, Integer>> testEdges = ImmutableSet.<Pair<String, Integer>>builder()
                .add(Pair.of("A", Integer.valueOf(1)))
                .add(Pair.of("A", Integer.valueOf(2)))
                .add(Pair.of("B", Integer.valueOf(1)))
                .add(Pair.of("C", Integer.valueOf(1)))
                .add(Pair.of("C", Integer.valueOf(3)))
                .add(Pair.of("C", Integer.valueOf(3)))
                .add(Pair.of("D", Integer.valueOf(1)))
                .add(Pair.of("D", Integer.valueOf(2)))
                .add(Pair.of("D", Integer.valueOf(3)))
                .build();

        RequestGraph<String, String> requestGraph = new RequestGraph(
                testEdges, Mockito.mock(Date.class), Mockito.mock(BrowserProfile.class));

        assertThat(
                requestGraph.getMeanFirstPartyNodeDegree(), is(2.0));
        assertThat(
                requestGraph.getMeanThirdPartyNodeDegree(), is(2.6666666666666665));
        assertThat(
                requestGraph.getMeanFirstPartyNodeDegreeAveragingTop(2), is(2.5));
        assertThat(
                requestGraph.getMeanThirdPartyNodeDegreeAveragingTop(1), is(4.0));
        assertThat(
                requestGraph.getDensity(), is(0.38095238095238093));
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
