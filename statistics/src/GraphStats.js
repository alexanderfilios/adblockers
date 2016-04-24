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
};

export default GraphStats;
