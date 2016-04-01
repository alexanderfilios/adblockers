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
  .controller('ForceDirectedController', ['$scope', function($scope) {
    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.NETWORK && scope.date,
      (loaded) => {if (loaded) fetchData($scope.date);});
    const fetchData = function(date) {
      $scope.connection.distinct(
        {crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)},
      ['firstParty', 'target'])
        .then(data => {
        const nodeDict = data.reduce(function(accumulator, current) {
          if (!(('s.' + current.firstParty) in accumulator)) {
            accumulator['s.' + current.firstParty] = {
              idx: Object.keys(accumulator).length,
              group: Object.keys(accumulator).length,
              name: current.firstParty,
              image: Utilities.constants.images.first
            };
          }
          if (!(('t.' + current.target) in accumulator)) {
            accumulator['t.' + current.target] = {
              idx: Object.keys(accumulator).length,
              group: Object.keys(accumulator).length,
              name: current.target,
              image: Utilities.constants.images.third
            };
          }
          return accumulator;
        }, {});
        const links = data.map(row => ({
          source: nodeDict['s.' + row.firstParty].idx,
          target: nodeDict['t.' + row.target].idx
        }));
        const nodes = Object.values(nodeDict);

        $scope.data = {
          nodes: nodes,
          links: links
        };
          $scope.$apply();
      });
    };

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
