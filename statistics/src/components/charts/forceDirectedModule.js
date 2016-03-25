/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import Utilities from '../../Utilities';

export default angular
  .module('forceDirected', ['ui.bootstrap'])
  .controller('ForceDirectedController', ['$scope', function($scope) {
    $scope.data = null;

    //$scope.$on('someEvent', (e) => console.log('aaaaaa'));
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.NETWORK,
      (loaded) => {if (loaded && $scope.data === null) fetchData();});

    const fetchData = function() {
      $scope.connection.distinct({}, ['firstParty', 'target']).then(data => {
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
      });
    };

  }])
  .directive('forceDirected', function($compile) {
    return {
      link: function(scope, element, attrs) {

        var width = 960,
          height = 500

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


        function createNodesLinks(json) {

          force
            .nodes(json.nodes)
            .links(json.links)
            .start();

          var link = svg.selectAll(".link")
            .data(json.links)
            .enter().append("line")
            .attr("class", "link");

          var node = svg.selectAll(".node")
            .data(json.nodes)
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
            .text(function(d) { return d.name });

          force.on("tick", function() {
            link.attr("x1", function(d) { return d.source.x; })
              .attr("y1", function(d) { return d.source.y; })
              .attr("x2", function(d) { return d.target.x; })
              .attr("y2", function(d) { return d.target.y; });

            node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
          });
        }

        if (typeof scope.data === 'string') {
          d3.json(scope.data, function(error, data) {
            if (error) throw error;
            createNodesLinks(data);
          });
        } else {
          Utilities
            .repeatPromise(null, () => scope.data !== null, 500)
            .then(() => createNodesLinks(scope.data), 2000);
        }


      }
    };
  })
  .name;
