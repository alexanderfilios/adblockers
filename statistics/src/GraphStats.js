/**
 * Created by alexandros on 4/6/16.
 */

import {Utilities} from 'adblocker-utils';
import {jStat} from 'jStat';
import {algorithms, functions, Graph, DiGraph} from 'jsnetworkx';

const GraphStats = function(data, entityDetails = null, undirected = true) {

  const self = this;
  self.entityDetails = entityDetails;

  self.data = data
    .map(row => {row.firstParty = row.heuristics.browserUri; return row;});

  const _getGraphLinks = function (data, srcToTgt = true) {
    const links = data
      // Only keep third-party requests
      .filter(row => Utilities.isTp(row))
      .reduce((cum, curr) => {
        if (curr[srcToTgt ? 'firstParty' : 'target'] in cum)
          cum[curr[srcToTgt ? 'firstParty' : 'target']][curr[srcToTgt ? 'target' : 'firstParty']] = 1;
        else
          cum[curr[srcToTgt ? 'firstParty' : 'target']] = {};
        return cum;
      }, {});
    for (const source in links) {
      links[source] = Object.keys(links[source]);
    }
    return links;

  };
  const _getGraphObject = function (data, srcToTgt = true) {
    console.log('Creating graph object');
    const links = _getGraphLinks(data, srcToTgt);
    const graph = undirected ? new Graph() : new DiGraph();
    graph.addNodesFrom(Object.keys(links).map(n => [('s.' + n), {f: true}]));
    graph.addNodesFrom(Object.values(links)
      .reduce((cum, curr) => cum.concat(curr), [])
      .map(n => [('t.' + n), {f: false}]));
    //graph.add
    graph.addEdgesFrom(jQuery.map(links, (connections, src) => connections.map(dst => ['s.' + src, 't.' + dst])));
    console.log('Graph object created!');
    return graph;
  };
  self.graph = null;
  self.entityGraph = null;

  self._entityMapping = null;
  self._replaceWithEntity = (url) => {
    if (self.entityDetails === null) {
      return url;
    } else if (self._entityMapping === null) {
      self._entityMapping = self.entityDetails
        .reduce((cum, cur) => {
          cum[cur.domain] = cur.admin_org || cur.tech_org || cur.regis_org;
          return cum;}, {});
    }
    return self._entityMapping[url
        .replace(/^t\./, '')
        .replace(/^s\./, '')
        .replace(/^www\./, '')] || url;
  };

  const _getEntityGraphObject = () => {
    //const entityMapping = entityDetails
    //  .reduce((cum, cur) => {
    //    cum[cur.domain] = cur.admin_org || cur.tech_org || cur.regis_org;
    //    return cum;}, {});
    //const entityEdges = Array.from(self.getGraph().edges())
    //  .map(e => [e[0], entityMapping[e[1].replace(/^t\./, '').replace(/^www\./, '')] || e[1]]);
    const entityEdges = Array.from(self.getGraph().edges())
      .map(e => [e[0], self._replaceWithEntity(e[1])]);
    const entityNodes = entityEdges.reduce((cum, cur) => cum.concat(cur), []);
    const entityGraph = undirected ? new Graph() : new DiGraph();
    entityGraph.addNodesFrom(entityNodes.filter(n => !n.startsWith('s.')).map(n => [n, {f: false}]));
    entityGraph.addNodesFrom(Array.from(self.graph.nodes()).filter(n => n.startsWith('s.')).map(n => [n, {f: true}]));
    entityGraph.addEdgesFrom(entityEdges);
    return entityGraph;
  };

  self.getLinks = (forFirstParties = true, forEntities = false) => Array
    .from(self.getGraph(forEntities).edges())
    .filter(e => (forFirstParties && e[0].startsWith('s.')) || (!forFirstParties && !e[0].startsWith('s.')))
    .map(e => ({source: e[0], target: e[1]}));

  self.isNotEmpty = (forEntities = false) => self.getGraph() !== null && self.getGraph(forEntities) !== null;
  self.getVertexDegrees = (forFirstParties = true, forEntities = false) =>
    Array.from(self.getGraph(forEntities)
      .degree(Array.from(self.getGraph(forEntities).nodesIter()), false))
      .filter(node => ((forFirstParties && node[0].startsWith('s.')) || !forFirstParties && !node[0].startsWith('s.')))
      .reduce((cum, curr) => {
        cum[curr[0]] = curr[1];
        return cum;
      }, {});

  self._rankDegree = [];
  self.getRankDegree = function(redirectionMappingData, firstPartyData) {
    if (self._rankDegree.length === 0) {
      const nodes = firstPartyData
        .map(d => ({
          rank: d.rank,
          url: redirectionMappingData
            .filter(r => r.original_url === d.url)
            .map(r => Utilities.parseUri(r.actual_url).host)[0] || Utilities.parseUri(d.url).host
        }));

      const vertexDegrees = self.getVertexDegrees(true);
      self._rankDegree = Object.keys(vertexDegrees)
        .map(d => ({url: d, degree: vertexDegrees[d]}))
        .map(d => ({degree: d.degree, url: d.url.replace(/^s\./, '')
          .replace(/^m\./, '')
          .replace(/^mobile\./, '')
          .replace(/^www\./, '')}))
        .map(d => nodes
          .filter(n => n.url.endsWith(d.url))
          .map(n => ({rank: n.rank, url: n.url, degree: d.degree})))
        .reduce((cum, cur) => cum.concat(cur));
    }
    return self._rankDegree;
  };


  self.getMeanDegreeOfNodes = (redirectionMappingData, firstPartyData, filter = () => true) => {
    return self.isNotEmpty()
      ? jStat.mean(self.getRankDegree(redirectionMappingData, firstPartyData)
      .filter(filter)
      .map(d => d.degree))
      : 0;
  };

  /**
   * If the redirectionMappingData and firstPartyData is given, we will get the rank and the original URL, as well
   * @param n
   * @param forFirstParties
   * @param forEntities
   * @param redirectionMappingData
   * @param firstPartyData
   * @returns {*}
   */
  self.getTopValues = (n, forFirstParties = true,
                       forEntities = false,
                       redirectionMappingData = null,
                       firstPartyData = null) => {
    let nodes = [];
    if (redirectionMappingData !== null && firstPartyData !== null) {
      nodes = self.getRankDegree(redirectionMappingData, firstPartyData);
    } else {
      const vertexDegrees = self.getVertexDegrees(forFirstParties, forEntities);
      nodes = Object.keys(vertexDegrees)
        .map(d => ({url: d, degree: vertexDegrees[d]}));
    }

    return nodes
      .map(d => jQuery.extend({}, d, {entity: self._replaceWithEntity(d.url)}))
      .reduce((cum, cur) => {
      if (cum.length < n) {
        return cum.concat(cur);
      } else {
        // Find the minimum element in the array so far
        const minIndex = cum
          .map((node, idx) => jQuery.extend({}, node, {idx: idx}))
          .reduce((cumMin, current) =>
            (cumMin.degree < current.degree) ? cumMin : current, {degree: +Infinity, idx: -1, domain: null})
          .idx;
        // If the min is less than the current, then replace it
        cum[minIndex] = cum[minIndex].degree > cur.degree ? cum[minIndex] : cur;
        return cum;
      }
    }, []);
  };

  self.getGraph = function(forEntities = false) {
    // The graph must be in any case calculated
    if (!self.graph && Array.isArray(self.data) && self.data.length > 0) {
      self.graph = _getGraphObject(data);
    }

    if (forEntities) {
      if (!self.entityGraph && Array.isArray(self.entityDetails)) {
        self.entityGraph = _getEntityGraphObject(self.entityDetails)
      }
      return self.entityGraph;
    } else {
      return self.graph;
    }
  };

  /**
   * Graph related metrics
   */
  self.getTopMeanDegree = (forFirstParties = true, topN = 10, forEntities = false) => self.isNotEmpty(forEntities)
    ? jStat.mean(self.getTopValues(topN, forFirstParties, forEntities).map(v => v.degree))
    : 0;
  self.getMeanDegree = (forFirstParties = true, forEntities = false) => self.isNotEmpty(forEntities)
    ? jStat.mean(Object.values(self.getVertexDegrees(forFirstParties, forEntities)))
    : 0;
  self.getStdevDegree = (forFirstParties = true, forEntities = false) => self.isNotEmpty(forEntities)
    ? jStat.stdev(Object.values(self.getVertexDegrees(forFirstParties, forEntities)))
    : 0;
  self.getDensity = (forEntities = false) => self.isNotEmpty(forEntities)
    ? functions.density(self.getGraph(forEntities))
    : 0;
  self.getBetweennessCentrality = (forEntities = false) => self.isNotEmpty(forEntities)
    ? Array.from(algorithms.betweennessCentrality(self.getGraph(forEntities)))
    .filter(n => n[0].startsWith('s.'))
    .reduce((cum, curr) => {
      cum[curr[0]] = curr[1];
      return cum;
    }, {})
    : {};
  self.getMeanBetweennessCentrality = (forEntities = false) => self.isNotEmpty(forEntities)
    ? jStat.mean(Object.values(self.getBetweennessCentrality(forEntities)))
    : 0;
  self.getDiameter = (forEntities = false) => self.isNotEmpty(forEntities)
    ? jStat.max(
    Array.from(algorithms.shortestPathLength(self.getGraph(forEntities)).values())
      .map(node => jStat.max(Array.from(node.values()))))
    : 0;

  /**
   * Request-related metrics
   */
  self.getMisclassifiedRequests = () => self.isNotEmpty()
    ? 100 * self.data
      .filter(r => Utilities.isTp(r)
        && !Utilities._urisMatch(r.heuristics.browserUri, r.source)
        && !Utilities._urisMatch(r.source, r.target))
      .length / self.data.length
    : 0;
  self.getUnrecognizedThirdPartyRequests = () => self.isNotEmpty()
    ? 100 * self.data
      .filter(r => Utilities.isTp(r)
        && !Utilities._urisMatch(r.heuristics.browserUri, r.source)
        && Utilities._urisMatch(r.source, r.target))
      .length / self.data.length
    : 0;
};

/*
 * Utility function to substitute the first party of each request with the actual redirected
 * When we are redirected by a website, the first party will differ from the newly-redirected website
 * This newly-redirected website will however be the source for many requests which will be categorized
 * as third-party requests although they are not. To address this issue, we will substitute the firstParty
 * with the new address.
 */
GraphStats.replaceRedirections = function(requestData, redirectionData) {
  console.log('Replacing redirections');
  // Re-format from [{original_url: ..., actual_url: ...}] to {original_url: actual_url}
  // for faster indexing
  const redirectionDict = redirectionData
    .reduce((cum, cur) => {
      cum[Utilities.parseUri(cur.original_url).host] = Utilities.parseUri(cur.actual_url).host;
      return cum;
    }, {});

  // Substitute the original first party with the redirected
  return requestData.map(req => jQuery.extend(
      {}, req, {firstParty: redirectionDict[req.firstParty] || req.firstParty}));
};

export default GraphStats;
