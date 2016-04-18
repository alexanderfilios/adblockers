/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
import moment from 'moment';
const d3 = require('d3');
import {Utilities} from 'adblocker-utils';

export default angular
  .module('forceDirected', ['ui.bootstrap'])
  .service('forceDirectedService', function() {
    this.getNodesAndLinks = function(graphStats, forFirstParties = true) {

      if (graphStats === undefined) {
        return null;
      }

      const nodeDict = Array.from(graphStats.graph.nodes())
        .map((e, idx) => ({
          idx: idx,
          group: idx,
          name: e,
          image: e.startsWith(forFirstParties ? 's.' : 't.') ? Utilities.constants.images.first : Utilities.constants.images.third,
        }))
        .reduce((cum, curr) => {
          cum[curr.name] = curr;
          return cum;
        }, {});
      return {
        links: graphStats.getLinks(forFirstParties).map(e => ({
          source: nodeDict[e.source].idx,
          target: nodeDict[e.target].idx
        })),
        nodes: Object.values(nodeDict)
      };
    }
  })
  .controller('ForceDirectedController', ['$scope', 'forceDirectedService', function($scope, forceDirectedService) {
    $scope.data = null;
    $scope.$watch(
        scope => scope.currentGraphStats,
        graphStats => $scope.data = forceDirectedService.getNodesAndLinks(graphStats)
    );
  }])
  .directive('forceDirected', function($compile) {
    return {
      link: function(scope, element, attrs) {
        scope.$watch('data', function () {
          // Clear previous data (if existing)
          element.find('svg').remove();
          element.find('div').remove();
          if (!scope.data || !('nodes' in scope.data) || !Array.isArray(scope.data.nodes) || scope.data.nodes.length === 0) {
            // If no data is present, display a message
            var message = d3.select(element[0])
              .append('div')
              .attr('class', 'message')
              .text('No data for ' + moment(scope.date).format('DD.MM.YYYY'));
            return;
          }

          var width = 960,
            height = 500;

          var svg = d3.select(element[0])
            .append("svg")
            .attr("class", "force-directed-style")
            .attr("width", width)
            .attr("height", height);

          var force = d3.layout.force()
            .gravity(0.05)
            .distance(100)
            .charge(-100)
            .size([width, height]);

            force
              .nodes(scope.data.nodes)
              .links(scope.data.links)
              .start();

            var link = svg.selectAll(".link")
              .data(scope.data.links)
              .enter().append("line")
              .attr("class", "link");

            var node = svg.selectAll(".node")
              .data(scope.data.nodes)
              .enter().append("g")
              .attr("class", "node")
              .call(force.drag);

            node.append("image")
              .attr("xlink:href", (data) => data.image)
              .attr("x", -8)
              .attr("y", -8)
              .attr("width", 16)
              .attr("height", 16);

            node.append("text")
              .attr("dx", 12)
              .attr("dy", ".35em")
              .text(function (d) {
                return d.name
              });

            force.on("tick", function () {
              link.attr("x1", function (d) {
                return d.source.x;
              })
                .attr("y1", function (d) {
                  return d.source.y;
                })
                .attr("x2", function (d) {
                  return d.target.x;
                })
                .attr("y2", function (d) {
                  return d.target.y;
                });

              node.attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
              });
            });

        });
      }
    };
  })
  .name;
