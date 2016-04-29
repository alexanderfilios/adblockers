/**
 * Created by alexandros on 4/6/16.
 */

import {Utilities} from 'adblocker-utils';
import {jStat} from 'jStat';
import {algorithms, functions, Graph, DiGraph} from 'jsnetworkx';

const GraphStats = function(data, undirected = true) {
  const self = this;
  self.data = data;

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
  self.graph = _getGraphObject(data, true);

  self.getLinks = (forFirstParties = true) => Array.from(self.graph.edges())
    .filter(e => e[0].startsWith(forFirstParties ? 's.' : 't.'))
    .map(e => ({source: e[0], target: e[1]}));

  self.isNotEmpty = () => Array.isArray(data) && data.length > 0;
  self.getVertexDegrees = (forFirstParties = true) =>
    Array.from(self.graph.degree(Array.from(self.graph.nodesIter()), false))
      .filter(node => node[0].startsWith(forFirstParties ? 's.' : 't.'))
      .reduce((cum, curr) => {
        cum[curr[0]] = curr[1];
        return cum;
      }, {});

  /**
   * Graph related metrics
   */
  self.getMeanDegree = (forFirstParties = true) => self.isNotEmpty()
    ? jStat.mean(Object.values(self.getVertexDegrees(forFirstParties)))
    : 0;
  self.getStdevDegree = (forFirstParties = true) => self.isNotEmpty()
    ? jStat.stdev(Object.values(self.getVertexDegrees(forFirstParties)))
    : 0;
  self.getDensity = () => self.isNotEmpty()
    ? functions.density(self.graph)
    : 0;
  self.getBetweennessCentrality = () => self.isNotEmpty()
    ? Array.from(algorithms.betweennessCentrality(self.graph))
    .filter(n => n[0].startsWith('s.'))
    .reduce((cum, curr) => {
      cum[curr[0]] = curr[1];
      return cum;
    }, {})
    : {};
  self.getMeanBetweennessCentrality = () => self.isNotEmpty()
    ? jStat.mean(Object.values(self.getBetweennessCentrality()))
    : 0;
  self.getDiameter = () => self.isNotEmpty()
    ? jStat.max(
    Array.from(algorithms.shortestPathLength(self.graph).values())
      .map(node => jStat.max(Array.from(node.values()))))
    : 0;

  /**
   * Request-related metrics
   */
  self.getMisclassifiedRequests = () => self.isNotEmpty()
    ? 100 * self.data
      .filter(r => Utilities.isTp(r)
        && !Utilities._urisMatch(r.firstParty, r.source)
        && !Utilities._urisMatch(r.source, r.target))
      .length / self.data.length
    : 0;
  self.getUnrecognizedThirdPartyRequests = () => self.isNotEmpty()
    ? 100 * self.data
      .filter(r => Utilities.isTp(r)
        && !Utilities._urisMatch(r.firstParty, r.source)
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
