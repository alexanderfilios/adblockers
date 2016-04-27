/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import moment from 'moment';
import {Utilities} from 'adblocker-utils';
import GraphStats from '../../GraphStats';

export default angular
  .module('lineChart', ['ui.bootstrap'])
  .controller('LineChartController', ['$scope', function($scope) {

    $scope.data = null;
    $scope.$watch(scope => scope.stats, stats => {
      if (Array.isArray(stats)) {
        const statDict = stats.reduce((cum, curr) => {
          cum[curr.name] = jQuery.extend({}, cum[curr.name]);
          cum[curr.name][curr.crawlDate] = jQuery.extend({date: curr.crawlDate}, cum[curr.name][curr.crawlDate]);
          cum[curr.name][curr.crawlDate][curr.instance] = curr.value;
          return cum;
        }, {});

        for (const stat in statDict) {
          statDict[stat] = {
            units: stat,
            charts: Object.values(statDict[stat])
            .sort((o1, o2) => new Date(o1.date) - new Date(o2.date))
          };
        }
        $scope.data = statDict;
      }

    });
  }])
  .directive('lineChart', function($compile) {
    return {
      link: function(scope, element, attrs) {

        scope.$watch('data', function () {
          // Clear previous data (if existing)
          element.find('svg').remove();
          element.find('div').remove();

          if (scope.data === null || !(attrs.metric in scope.data)) {
            var message = d3.select(element[0])
              .append('div')
              .attr('class', 'message')
              .text('No data loaded yet');
            return;
          }
          const data = scope.data[attrs.metric];

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
            //.interpolate("basis")
            .interpolate("cardinal")
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

          color.domain(d3.keys(data.charts[0]).filter(function (key) {
            return key !== "date";
          }));

          data.charts.forEach(function (d) {
            d.date = parseDate(d.date);
          });

          var cities = color.domain().map(function (name) {
            return {
              name: name,
              values: data.charts.map(function (d) {
                return {date: d.date, val: +d[name]};
              })
            };
          });

          x.domain(d3.extent(data.charts, function (d) {
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
            .text(data.units);

          var series = svg.selectAll(".series")
            .data(cities)
            .enter().append("g")
            .attr("class", "series");


          // Append points
          var points = svg.selectAll(".point")
            .data(cities
              .map(c => c.values.map(v => {v.name = c.name; return v;}))
              .reduce((cum, curr) => cum.concat(curr)))
            .enter().append("svg:circle")
            .attr("stroke", function(d, i) {return color(d.name);})
            .attr("fill", function(d, i) { return "black"; })
            .attr("cx", function(d, i) { return x(d.date) })
            .attr("cy", function(d, i) { return y(d.val) })
            .attr("r", function(d, i) { return 3 })
            .on("mouseover", function(d, i) {
              jQuery(".dodo[x=\"" + x(d.date) + "\"][y=\"" + y(d.val) + "\"]").show();
            })
            .on("mouseout", function(d, i) {
              jQuery(".dodo[x=\"" + x(d.date) + "\"][y=\"" + y(d.val) + "\"]").hide();
            });

           //Append text with values
          svg.selectAll(".dodo")
            .data(cities
              .map(c => c.values)
              .reduce((cum, curr) => cum.concat(curr)))
            .enter().append("text")
            .attr("class", "dodo")
            .attr("x", function(d) { return x(d.date); })
            .attr("y", function(d) { return y(d.val); })
            .attr("dx", ".71em")
            .attr("dy", "-.7em")
            .attr("display", "none")
            .text(function(d) { return d.val.toFixed(3);});


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
            //.attr("x", 3)
            //.attr("dy", ".35em")
            //.text(function (d) {
            //  return d.name;
            //});

          var legend = svg.selectAll(".legend")
            .data(color.domain().slice().reverse())
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function (d, i) {
              return "translate(0," + i * 20 + ")";
            });

          legend.append("rect")
            .attr("x", width - 18)
            .attr("width", 18)
            .attr("height", 18)
            .style("fill", color);

          legend.append("text")
            .attr("x", width - 24)
            .attr("y", 9)
            .attr("dy", ".35em")
            .style("text-anchor", "end")
            .text(function (d) {
              return d;
            });

        });
      }
    };
  })
  .name;
