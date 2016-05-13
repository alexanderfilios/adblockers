/**
 * Created by alexandros on 4/6/16.
 */

import {Utilities} from 'adblocker-utils';
import {jStat} from 'jStat';
import {algorithms, functions, Graph, DiGraph} from 'jsnetworkx';

const GraphStats = function(data, entityDetails, undirected = true) {
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

  const _getEntityGraphObject = (entityDetails) => {
    const entityMapping = entityDetails
      .reduce((cum, cur) => {
        cum[cur.domain] = cur.admin_org || cur.tech_org || cur.regis_org;
        return cum;}, {});
    const entityEdges = Array.from(self.getGraph().edges())
      .map(e => [e[0], entityMapping[e[1].replace(/^t\./, '').replace(/^www\./, '')] || e[1]]);

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

  self.isNotEmpty = (forEntities = false) => self.getGraph(forEntities) !== null;
  self.getVertexDegrees = (forFirstParties = true, forEntities = false) =>
    Array.from(self.getGraph(forEntities)
      .degree(Array.from(self.getGraph(forEntities).nodesIter()), false))
      .filter(node => ((forFirstParties && node[0].startsWith('s.')) || !forFirstParties && !node[0].startsWith('s.')))
      .reduce((cum, curr) => {
        cum[curr[0]] = curr[1];
        return cum;
      }, {});

  self._rankDegree = [];
  self.getRankDegree = function(nodes) {
    if (self._rankDegree.length === 0) {
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


  self.getMeanDegreeOfNodes = function(nodes, filter = () => true) {
    return jStat.mean(self.getRankDegree(nodes)
      .filter(filter)
      .map(d => d.degree));
  };

  const _getTopValues = (nodes, n) => {
    return nodes.reduce((cum, cur) => {
      if (cum.length < n) {
        return cum.concat(cur);
      } else {
        // Find the minimum element in the array so far
        const minIndex = cum
          .map((val, idx) => ({val: val, idx: idx}))
          .reduce((cumMin, current) =>
            (cumMin.val < current.val) ? cumMin : current, {val: +Infinity, idx: -1})
          .idx;
        // If the min is less than the current, then replace it
        cum[minIndex] = Math.max(cum[minIndex], cur);
        return cum;
      }
    }, []);
  };

  self.getGraph = function(forEntities = false) {
    // The graph must be in any case calculated
    if (!self.graph && Array.isArray(self.data)) {
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
    ? jStat.mean(_getTopValues(Object.values(self.getVertexDegrees(forFirstParties, forEntities)), topN))
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
