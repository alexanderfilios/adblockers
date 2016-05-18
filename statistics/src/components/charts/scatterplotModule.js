/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
import moment from 'moment';
const d3 = require('d3');
import {Utilities} from 'adblocker-utils';

export default angular
  .module('scatterplot', ['ui.bootstrap'])
  .service('scatterplotService', function() {
    const self = this;
    self.getStackedBar = function(graphStats) {
      if (jQuery.isEmptyObject(graphStats)) {
        return null;
      }

      const categories = ['image', 'application', 'text', 'font', 'video', 'binary', 'content', 'audio'];
      const bars = Object.keys(graphStats)
        .reduce((cum, instance) => cum.concat(([{barTitle: instance, data: graphStats[instance].data}])), [])
        .map(bar => categories.reduce(function (accumulator, current) {
          accumulator[current] = bar.data.filter(row => row.contentType.split('/')[0] === current).length;
          return accumulator;
        }, {barTitle: bar.barTitle}));

      return {units: 'Requests', bars: bars};
    }
  })
  .controller('ScatterplotController', ['$scope', 'scatterplotService', 'csvService', function($scope, scatterplotService, csvService) {
    $scope.data = null;
    $scope.$watch(
      (scope) => scope.graphStats,
      (graphStats) => {
        let redirections = [];
        $scope.connection._find($scope.connection._redirectionMappingTable)
          .then(data => {redirections = data; return $scope.connection._find($scope.connection._firstPartyTable);})
          .then(data => {
            // Substitute URLs from first-party collection with the actual URLs from redirection-mapping collection
            const redirectedDomains = data
              .map(d => ({
                rank: d.rank,
                url: redirections
                  .filter(r => r.original_url === d.url)
                  .map(r => Utilities.parseUri(r.actual_url).host)[0] || Utilities.parseUri(d.url).host
              }));
            $scope.data = graphStats.getRankDegree(redirections, data);
            return csvService.getCsvDataInRange($scope.data, $scope.data.map(d => d.rank));
          }).
          then(data => {
            $scope.csvData = data;
            $scope.$apply();
          });
      }
    );
  }])
  .directive('scatterplot', function($compile) {
    return {
      link: function(scope, element, attrs) {
        scope.$watch('data', function () {
          // Clear previous data (if existing)
          element.find('svg').remove();
          element.find('div').remove();
          if (!scope.data || !Array.isArray(scope.data) || scope.data.length === 0) {
            // If no data is present, display a message
            var message = d3.select(element[0])
              .append('div')
              .attr('class', 'message')
              .text('No data for ' + moment(scope.date).format('DD.MM.YYYY'));
            return;
          }
            d3.select(element[0])
              .append('a')
              .attr('class', 'btn btn-primary')
              .text('Download scatterplot data')
              .attr('href', window.URL.createObjectURL(scope.csvData))
              .attr('download', 'scatterplot');

          var margin = {top: 20, right: 20, bottom: 30, left: 40},
            width = 960 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

          var x = d3.scale.linear()
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

          var svg = d3.select(element[0]).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

          createScatterplot(scope.data);
          function createScatterplot(data, error) {

            if (error) throw error;

            data.forEach(function (d) {
              d.degree = +d.degree;
              d.rank = +d.rank;
            });

            x.domain(d3.extent(data, function (d) {
              return d.rank;
            })).nice();
            y.domain(d3.extent(data, function (d) {
              return d.degree;
            })).nice();

            svg.append("g")
              .attr("class", "x axis")
              .attr("transform", "translate(0," + height + ")")
              .call(xAxis)
              .append("text")
              .attr("class", "label")
              .attr("x", width)
              .attr("y", -6)
              .style("text-anchor", "end")
              .text("Rank");

            svg.append("g")
              .attr("class", "y axis")
              .call(yAxis)
              .append("text")
              .attr("class", "label")
              .attr("transform", "rotate(-90)")
              .attr("y", 6)
              .attr("dy", ".71em")
              .style("text-anchor", "end")
              .text("Node degree")

            svg.selectAll(".dot")
              .data(data)
              .enter().append("circle")
              .attr("class", "dot")
              .attr("r", 2.5)
              .attr("cx", function (d) {
                return x(d.rank);
              })
              .attr("cy", function (d) {
                return y(d.degree);
              })
              .style("fill", function (d) {
                return color(d.species);
              });

            //var legend = svg.selectAll(".legend")
            //  .data(color.domain())
            //  .enter().append("g")
            //  .attr("class", "legend")
            //  .attr("transform", function (d, i) {
            //    return "translate(0," + i * 20 + ")";
            //  });
            //
            //legend.append("rect")
            //  .attr("x", width - 18)
            //  .attr("width", 18)
            //  .attr("height", 18)
            //  .style("fill", color);
            //
            //legend.append("text")
            //  .attr("x", width - 24)
            //  .attr("y", 9)
            //  .attr("dy", ".35em")
            //  .style("text-anchor", "end")
            //  .text(function (d) {
            //    return d;
            //  });


        }

        });
      }
    };
  })
  .name;
