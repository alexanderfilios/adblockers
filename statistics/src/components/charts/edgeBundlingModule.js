/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import moment from 'moment';
import Utilities from '../../Utilities';

export default angular
  .module('edgeBundling', ['ui.bootstrap'])
  .controller('EdgeBundlingController', ['$scope', function($scope) {

    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.BUNDLE && scope.date,
      (loaded) => {if (loaded) fetchData($scope.date);});
    const fetchData = function(date) {
      $scope.connection.find({crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
        .then(data => {
          data = data
          // Only keep third-party requests
            .filter(row => row.target !== row.source)
            .map(row => Object({
              // Use first party as the source. Replace reserved char '.' with ','.
              source: 's.' + row.firstParty.replace(/\./g, ','),
              target: 't.' + row.target.replace(/\./g, ',')
            }));

          $scope.data = jQuery.unique(data.map(row => row.source).concat(data.map(row => row.target)))
            .map(source => Object({
              name: source,
              imports: jQuery.unique(data.filter(r => r.source === source).map(r => r.target))
            }));
          $scope.$apply();
        }
      );
    };

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
