/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import moment from 'moment';
import {Utilities} from 'adblocker-utils';
import {jStat} from 'jStat';
import {algorithms, functions, Graph, DiGraph} from 'jsnetworkx';
import GraphStats from '../../GraphStats';



//const GraphStats = function(data, undirected = true) {
//  const self = this;
//
//  const _getGraphLinks = function (data, srcToTgt = true) {
//    const links = data
//      // Only keep third-party requests
//      .filter(row => Utilities.isTp(row))
//      .reduce((cum, curr) => {
//        if (curr[srcToTgt ? 'firstParty' : 'target'] in cum)
//          cum[curr[srcToTgt ? 'firstParty' : 'target']][curr[srcToTgt ? 'target' : 'firstParty']] = 1;
//        else
//          cum[curr[srcToTgt ? 'firstParty' : 'target']] = {};
//        return cum;
//      }, {});
//    for (const source in links) {
//      links[source] = Object.keys(links[source]);
//    }
//    return links;
//
//  };
//  const _getGraphObject = function (data, srcToTgt = true) {
//    const links = _getGraphLinks(data, srcToTgt);
//    const graph = undirected ? new Graph() : new DiGraph();
//    graph.addNodesFrom(Object.keys(links).map(n => [('s.' + n), {f: true}]));
//    graph.addNodesFrom(Object.values(links)
//      .reduce((cum, curr) => cum.concat(curr), [])
//      .map(n => [('t.' + n), {f: false}]));
//    //graph.add
//    graph.addEdgesFrom(jQuery.map(links, (connections, src) => connections.map(dst => ['s.' + src, 't.' + dst])));
//    return graph;
//  };
//  self.graph = _getGraphObject(data, true);
//
//  self.isNotEmpty = () => Array.isArray(data) && data.length > 0;
//  self.getVertexDegrees = (forFirstParties = true) =>
//    Array.from(self.graph.degree(Array.from(self.graph.nodesIter()), false))
//      .filter(node => node[0].startsWith(forFirstParties ? 's.' : 't.'))
//      .reduce((cum, curr) => {
//        cum[curr[0]] = curr[1];
//        return cum;
//      }, {});
//
//  self.getMeanDegree = (forFirstParties = true) => self.isNotEmpty()
//    ? jStat.mean(Object.values(self.getVertexDegrees(forFirstParties)))
//    : 0;
//  self.getStdevDegree = (forFirstParties = true) => self.isNotEmpty()
//    ? jStat.stdev(Object.values(self.getVertexDegrees(forFirstParties)))
//    : 0;
//  self.getDensity = () => self.isNotEmpty()
//    ? functions.density(self.graph)
//    : 0;
//  self.getBetweennessCentrality = () => self.isNotEmpty()
//    ? Array.from(algorithms.betweennessCentrality(self.graph))
//    .filter(n => n[0].startsWith('s.'))
//    .reduce((cum, curr) => {
//      cum[curr[0]] = curr[1];
//      return cum;
//    }, {})
//    : 0;
//  self.getDiameter = () => jStat.max(
//    Array.from(algorithms.shortestPathLength(self.graph).values())
//      .map(node => jStat.max(Array.from(node.values()))))
//};

window.from = Array.from;
window.jStat = jStat;
window.functions = functions;
window.algorithms = algorithms;
window.Graph = Graph;

export default angular
  .module('lineChart', ['ui.bootstrap'])
  .controller('LineChartController', ['$scope', function($scope) {

    //$scope.connection._clearCollection($scope.connection._statsTable);

$scope.connection._host = '127.0.0.1';

    $scope.connection.find({crawlDate: '04/03/2016'}).then(
        data => {
          let graphStats = new GraphStats(data);
          window.graph = graphStats;

          console.log(graphStats.getVertexDegrees());
          console.log('mean degree: ' + graphStats.getMeanDegree());
          console.log('stdev degree: ' + graphStats.getStdevDegree());
          console.log('density: ' + graphStats.getDensity());
          //console.log('graph diameter: ' + graphStats.getDiameter());
          //const start = new Date();
          //console.log('betweenness centrality:');
          //console.log(graphStats.getBetweennessCentrality());
          //console.log('time to calculate BC: ' + (moment().diff(moment(start)) / 1000) + ' sec')
      }
    );

//$scope.connection.clearStats();

    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.LINE_CHART,
      (loaded) => {if (loaded) fetchData();});
    const fetchData = function() {
      let dataPerDate = {};

      Utilities.executeSerially(
        [
          {
            name: 'first-meansafada',
            title: 'First mean',
            calculator: data => new GraphStats(data).getMeanDegree()
          },
          //{
          //  name: 'first-stdev',
          //  title: 'First standard deviation',
          //  calculator: (data) => GraphUtils.getStdevDegree(data, true)
          //},
          //{
          //  name: 'third-mean',
          //  title: 'Third mean',
          //  calculator: (data) => GraphUtils.getMeanDegree(data, false)
          //},
          //{
          //  name: 'third-stdev',
          //  title: 'Third standard deviation',
          //  calculator: (data) => GraphUtils.getStdevDegree(data, false)
          //},
          //{
          //  name: 'density',
          //  title: 'Graph density',
          //  calculator: (data) => GraphUtils.getGraphDensity(data)
          //}
        ],
        (input, output) => output.forEach(dataOfDay => {
            dataPerDate[dataOfDay.date] = jQuery.extend({date: dataOfDay.date}, dataPerDate[dataOfDay.date]);
            dataPerDate[dataOfDay.date][input.title] = dataOfDay.value;
          }),
        (input) => $scope.connection.findOrCalculateStats(input.name, input.calculator, '03/30/2016', '04/03/2016')
      ).then(() => {
          $scope.data = {
            units: 'Requests',
            charts: Object.values(dataPerDate)
          };

          $scope.$apply();
        });
    };
  }])
  .directive('lineChart', function($compile) {
    return {
      link: function(scope, element, attrs) {

        scope.$watch('data', function () {
          // Clear previous data (if existing)
          element.find('svg').remove();
          element.find('div').remove();
          if (scope.data === null || !Array.isArray(scope.data.charts) || scope.data.charts.length === 0) {
            // If no data is present, display a message
            var message = d3.select(element[0])
              .append('div')
              .attr('class', 'message')
              .text('No data for ' + moment(scope.date).format('DD.MM.YYYY'));
            return;
          }

          var margin = {top: 20, right: 80, bottom: 30, left: 50},
            width = 960 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

          var parseDate = d3.time.format(Utilities.constants.D3_DATE_FORMAT).parse;

          var x = d3.time.scale()
            .range([0, width]);

          var y = d3.scale.linear()
            .range([height, 0]);

          var color = d3.scale.category10();

          var xAxis = d3.svg.axis()
            .scale(x)
            .orient("bottom");

          var yAxis = d3.svg.axis()
            .scale(y)
            .orient("left");

          var line = d3.svg.line()
            .interpolate("basis")
            .x(function (d) {
              return x(d.date);
            })
            .y(function (d) {
              return y(d.val);
            });

          var svg = d3.select(element[0])
            .append("svg")
            .attr("class", "line-chart-style")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

          color.domain(d3.keys(scope.data.charts[0]).filter(function (key) {
            return key !== "date";
          }));

          scope.data.charts.forEach(function (d) {
            d.date = parseDate(d.date);
          });

          var cities = color.domain().map(function (name) {
            return {
              name: name,
              values: scope.data.charts.map(function (d) {
                return {date: d.date, val: +d[name]};
              })
            };
          });

          x.domain(d3.extent(scope.data.charts, function (d) {
            return d.date;
          }));

          y.domain([
            d3.min(cities, function (c) {
              return d3.min(c.values, function (v) {
                return v.val;
              });
            }),
            d3.max(cities, function (c) {
              return d3.max(c.values, function (v) {
                return v.val;
              });
            })
          ]);

          svg.append("g")
            .attr("class", "x axis")
            .attr("transform", "translate(0," + height + ")")
            .call(xAxis);

          svg.append("g")
            .attr("class", "y axis")
            .call(yAxis)
            .append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 6)
            .attr("dy", ".71em")
            .style("text-anchor", "end")
            .text(scope.data.units);

          var series = svg.selectAll(".series")
            .data(cities)
            .enter().append("g")
            .attr("class", "series");

          series.append("path")
            .attr("class", "line")
            .attr("d", function (d) {
              return line(d.values);
            })
            .style("stroke", function (d) {
              return color(d.name);
            });

          series.append("text")
            .datum(function (d) {
              return {name: d.name, value: d.values[d.values.length - 1]};
            })
            .attr("transform", function (d) {
              return "translate(" + x(d.value.date) + "," + y(d.value.val) + ")";
            })
            .attr("x", 3)
            .attr("dy", ".35em")
            .text(function (d) {
              return d.name;
            });

        });
      }
    };
  })
  .name;
