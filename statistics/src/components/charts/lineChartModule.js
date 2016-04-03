/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import moment from 'moment';
import {Utilities} from 'adblocker-utils';

export default angular
  .module('lineChart', ['ui.bootstrap'])
  .controller('LineChartController', ['$scope', function($scope) {

    //$scope.connection._clearCollection($scope.connection._statsTable);

    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.LINE_CHART,
      (loaded) => {if (loaded) fetchData();});
    const fetchData = function() {
      let dataPerDate = {};

      Utilities.executeSerially(
        [
          {name: 'reqs', title: 'Reqs', calculator: (data) => data.length},
          {name: 'reqsplus', title: 'Reqs plus', calculator: (data) => data.length + 1000},
        ],
        (input, output) => output.forEach(dataOfDay => {
            dataPerDate[dataOfDay.date] = jQuery.extend({date: dataOfDay.date}, dataPerDate[dataOfDay.date]);
            dataPerDate[dataOfDay.date][input.title] = dataOfDay.value;
          }),
        (input) => $scope.connection.findOrCalculateStats(input.name, input.calculator, '03/29/2016')
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
