/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import moment from 'moment';
import {Utilities} from 'adblocker-utils';

export default angular
  .module('edgeBundling', ['ui.bootstrap'])
  .service('edgeBundlingService', function() {
    const self = this;
    self._formatName = (str) => str.replace(/\./g, (substr, pos) => pos === 1 ? '.' : ',');
    self.getEdgeBundling = function(graphStats) {
      if (graphStats === undefined) {
        return null;
      }

      return Array.from(graphStats.graph.nodes())
        .map(e => ({
          name: self._formatName(e),
          imports: Array.from(graphStats.graph.neighbors(e)).map(n => self._formatName(n))
        }));
    }
  })
  .controller('EdgeBundlingController', ['$scope', 'edgeBundlingService', function($scope, edgeBundlingService) {
    $scope.data = null;
    $scope.$watch(
      scope => scope.currentGraphStats,
      graphStats => $scope.data = edgeBundlingService.getEdgeBundling(graphStats)
    );
  }])
  .directive('edgeBundling', function($compile) {
    return {
      link: function(scope, element, attrs) {

        scope.$watch('data', function () {
          // Clear previous data (if existing)
          element.find('svg').remove();
          element.find('div').remove();
          if (!Array.isArray(scope.data) || scope.data.length === 0) {
            // If no data is present, display a message
            var message = d3.select(element[0])
              .append('div')
              .attr('class', 'message')
              .text('No data for ' + moment(scope.date).format('DD.MM.YYYY'));
            return;
          }

          var diameter = 960,
            radius = diameter / 2,
            innerRadius = radius - 120;

          var cluster = d3.layout.cluster()
            .size([360, innerRadius])
            .sort(null)
            .value(function (d) {
              return d.size;
            });

          var bundle = d3.layout.bundle();

          var line = d3.svg.line.radial()
            .interpolate("bundle")
            .tension(.85)
            .radius(function (d) {
              return d.y;
            })
            .angle(function (d) {
              return d.x / 180 * Math.PI;
            });


          var svg = d3.select(element[0]).append("svg")
            .attr("width", diameter)
            .attr("height", diameter)
            .attr("class", "edge-bundling-style")
            .append("g")
            .attr("transform", "translate(" + radius + "," + radius + ")");

          var link = svg.append("g").selectAll(".link"),
            node = svg.append("g").selectAll(".node");

          var nodes = cluster.nodes(packageHierarchy(scope.data)),
            links = packageImports(nodes);

          link = link
            .data(bundle(links))
            .enter().append("path")
            .each(function (d) {
              d.source = d[0], d.target = d[d.length - 1];
            })
            .attr("class", "link")
            .attr("d", line);

          node = node
            .data(nodes.filter(function (n) {
              return !n.children;
            }))
            .enter().append("text")
            .attr("class", "node")
            .attr("dy", ".31em")
            .attr("transform", function (d) {
              return "rotate(" + (d.x - 90) + ")translate(" + (d.y + 8) + ",0)" + (d.x < 180 ? "" : "rotate(180)");
            })
            .style("text-anchor", function (d) {
              return d.x < 180 ? "start" : "end";
            })
            .text(function (d) {
              return d.key;
            })
            .on("mouseover", mouseovered)
            .on("mouseout", mouseouted);

          function mouseovered(d) {
            node
              .each(function (n) {
                n.target = n.source = false;
              });

            link
              .classed("link--target", function (l) {
                if (l.target === d) return l.source.source = true;
              })
              .classed("link--source", function (l) {
                if (l.source === d) return l.target.target = true;
              })
              .filter(function (l) {
                return l.target === d || l.source === d;
              })
              .each(function () {
                this.parentNode.appendChild(this);
              });

            node
              .classed("node--target", function (n) {
                return n.target;
              })
              .classed("node--source", function (n) {
                return n.source;
              });
          }

          function mouseouted(d) {
            link
              .classed("link--target", false)
              .classed("link--source", false);

            node
              .classed("node--target", false)
              .classed("node--source", false);
          }

          d3.select(self.frameElement).style("height", diameter + "px");

// Lazily construct the package hierarchy from class names.
          function packageHierarchy(classes) {
            var map = {};

            function find(name, data) {
              var node = map[name], i;
              if (!node) {
                node = map[name] = data || {name: name, children: []};
                if (name.length) {
                  node.parent = find(name.substring(0, i = name.lastIndexOf(".")));
                  node.parent.children.push(node);
                  node.key = name.substring(i + 1);
                }
              }
              return node;
            }

            classes.forEach(function (d) {
              find(d.name, d);
            });

            return map[""];
          }

// Return a list of imports for the given array of nodes.
          function packageImports(nodes) {
            var map = {},
              imports = [];

            // Compute a map from name to node.
            nodes.forEach(function (d) {
              map[d.name] = d;
            });

            // For each import, construct a link from the source to target node.
            nodes.forEach(function (d) {
              if (d.imports) d.imports.forEach(function (i) {
                imports.push({source: map[d.name], target: map[i]});
              });
            });

            return imports;
          }


        });
      }
    };
  })
  .name;
