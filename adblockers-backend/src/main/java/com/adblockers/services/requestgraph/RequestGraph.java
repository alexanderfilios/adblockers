package com.adblockers.services.requestgraph;

import com.adblockers.entities.*;
import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.data.util.Pair;


import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by alexandrosfilios on 18/09/16.
 */
public class RequestGraph<S, T> {

    private static final Logger LOGGER = Logger.getLogger(RequestGraph.class);

    private UndirectedGraph<RequestGraphNode, DefaultEdge> requestGraph;
    private Date crawlDate;
    private BrowserProfile browserProfile;
    private RequestGraphType requestGraphType;

    public enum RequestGraphType {
        ENTITY_REQUEST_GRAPH,
        DOMAIN_REQUEST_GRAPH,
        UNDEFINED
    }

    public <S, T> RequestGraph(Set<Pair<S, T>> edges, Date crawlDate, BrowserProfile browserProfile) {
        this.crawlDate = crawlDate;
        this.browserProfile = browserProfile;
        this.setRequestGraphType(edges);
        this.requestGraph = this.createGraph(edges);

        LOGGER.info("Domain request graph created. "
                + "Type: " + getRequestGraphType() + ", "
                + "Crawl Date: " + HttpRequestRecord.DATE_FORMAT.format(getCrawlDate()) + ", "
                + "Browser Profile: " + getBrowserProfile());
    }

    private <S, T> UndirectedGraph<RequestGraphNode, DefaultEdge> createGraph(Set<Pair<S, T>> edges) {
        UndirectedGraph<RequestGraphNode, DefaultEdge> graph = new SimpleGraph(DefaultEdge.class);

        Map<S, RequestGraphNode<S>> firstParties = new HashMap<>();
        Map<T, RequestGraphNode<T>> thirdParties = new HashMap<>();

        edges.forEach(edge -> {
            firstParties.putIfAbsent(edge.getFirst(), new RequestGraphNode<S>(edge.getFirst(), true));
            thirdParties.putIfAbsent(edge.getSecond(), new RequestGraphNode<T>(edge.getSecond(), false));

            RequestGraphNode<S> firstParty = firstParties.get(edge.getFirst());
            RequestGraphNode<T> thirdParty = thirdParties.get(edge.getSecond());

            graph.addVertex(firstParty);
            graph.addVertex(thirdParty);
            graph.addEdge(firstParty, thirdParty);
        });

        return graph;
    }

    public Double getDensity() {
        Integer edges = requestGraph.edgeSet().size();
        Integer vertices = requestGraph.vertexSet().size();
        return (double) 2 * edges / (vertices * (vertices - 1));
    }

    private List<RequestGraphNode> getFirstPartyNodes() {
        return requestGraph.vertexSet().stream()
                .filter(node -> node.getIsFirstParty())
                .collect(Collectors.toList());
    }

    private List<RequestGraphNode> getThirdPartyNodes() {
        return requestGraph.vertexSet().stream()
                .filter(node -> !node.getIsFirstParty())
                .collect(Collectors.toList());
    }

    private List<Integer> getFirstPartyNodeDegrees() {
        return getFirstPartyNodes().stream()
                .map(node -> requestGraph.edgesOf(node).size())
                .collect(Collectors.toList());
    }
    private List<Integer> getThirdPartyNodeDegrees() {
        return getThirdPartyNodes().stream()
                .map(node -> requestGraph.edgesOf(node).size())
                .collect(Collectors.toList());
    }
    public Double getMeanFirstPartyNodeDegree() {
        List<Integer> firstPartyNodeDegrees = getFirstPartyNodeDegrees();
        return firstPartyNodeDegrees.stream()
                .collect(Collectors.summingDouble(d -> (double) d))
                / firstPartyNodeDegrees.size();
    }

    public Double getMeanThirdPartyNodeDegree() {
        List<Integer> thirdPartyNodeDegrees = getThirdPartyNodeDegrees();
        return thirdPartyNodeDegrees.stream()
                .mapToDouble(d -> (double) d).sum() / thirdPartyNodeDegrees.size();
    }

    public Double getMeanFirstPartyNodeDegreeAveragingTop(Integer maxSize) {
        return getFirstPartyNodeDegrees().stream()
                .sorted((d1, d2) -> d2 - d1)
                .limit(maxSize)
                .mapToDouble(d -> (double) d).sum() / maxSize;

    }

    public Double getMeanThirdPartyNodeDegreeAveragingTop(Integer maxSize) {
        return getThirdPartyNodeDegrees().stream()
                .sorted((d1, d2) -> d2 - d1)
                .limit(maxSize)
                .mapToDouble(d -> (double) d).sum() / maxSize;
    }

    public Metric getMetric(Metric.MetricType metricType) {
        switch (metricType) {
            case FPD_DEGREE_MEAN:
                return Metric.from(getCrawlDate(), getMeanFirstPartyNodeDegree(), Metric.MetricType.FPD_DEGREE_MEAN, getRequestGraphType(), getBrowserProfile());
            case FPD_DEGREE_MEAN_TOP_10:
                return Metric.from(getCrawlDate(), getMeanFirstPartyNodeDegreeAveragingTop(10), Metric.MetricType.FPD_DEGREE_MEAN_TOP_10, getRequestGraphType(), getBrowserProfile());
            case FPD_DEGREE_MEAN_TOP_1:
                return Metric.from(getCrawlDate(), getMeanFirstPartyNodeDegreeAveragingTop(1), Metric.MetricType.FPD_DEGREE_MEAN_TOP_1, getRequestGraphType(), getBrowserProfile());
            case TPD_DEGREE_MEAN:
                return Metric.from(getCrawlDate(), getMeanThirdPartyNodeDegree(), Metric.MetricType.TPD_DEGREE_MEAN, getRequestGraphType(), getBrowserProfile());
            case TPD_DEGREE_MEAN_TOP_10:
                return Metric.from(getCrawlDate(), getMeanThirdPartyNodeDegreeAveragingTop(10), Metric.MetricType.TPD_DEGREE_MEAN_TOP_10, getRequestGraphType(), getBrowserProfile());
            case TPD_DEGREE_MEAN_TOP_1:
                return Metric.from(getCrawlDate(), getMeanThirdPartyNodeDegreeAveragingTop(1), Metric.MetricType.TPD_DEGREE_MEAN_TOP_1, getRequestGraphType(), getBrowserProfile());
            case DENSITY:
                return Metric.from(getCrawlDate(), getDensity(), Metric.MetricType.DENSITY, getRequestGraphType(), getBrowserProfile());
            default:
                return null;
        }
    }


    public Date getCrawlDate() {
        return crawlDate;
    }
    public BrowserProfile getBrowserProfile() {
        return browserProfile;
    }

    private <S, T> void setRequestGraphType(Set<Pair<S, T>> edges) {
        if (edges.stream().allMatch(
                e -> e.getFirst().getClass().equals(Url.class)
                        && e.getSecond().getClass().equals(Url.class))) {
            this.requestGraphType = RequestGraphType.DOMAIN_REQUEST_GRAPH;
        } else if (edges.stream().allMatch(
                e -> e.getFirst().getClass().equals(Url.class)
                && e.getSecond().getClass().equals(LegalEntity.class))) {
            this.requestGraphType = RequestGraphType.ENTITY_REQUEST_GRAPH;
        } else {
            this.requestGraphType = RequestGraphType.UNDEFINED;
        }

    }
    public RequestGraphType getRequestGraphType() {
        return requestGraphType;
    }

}
