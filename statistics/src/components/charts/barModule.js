/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
import moment from 'moment';

const d3 = require('d3');
import Utilities from '../../Utilities';

export default angular
  .module('bar', ['ui.bootstrap'])
  .controller('BarController', ['$scope', function($scope) {

    const colors = {
      first: 'lightblue',
      third: 'green'
      };
    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.BAR && scope.date,
      (loaded) => {if (loaded) fetchData($scope.date);});
    const fetchData = function(date) {
      $scope.connection.find({crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
        .then(data => {
        $scope.data = jQuery.unique(data.map(row => row.firstParty))
          .map(firstParty => Object({
            name: firstParty,
            firsts: jQuery.unique(data
              .filter(row => row.firstParty === firstParty)
              .filter(row => Utilities.isFalseFp(row))
              .map(row => row.source)).length,
            thirds: jQuery.unique(data
              .filter(row => row.firstParty === firstParty)
              .filter(row => Utilities.isTp(row))
              .map(row => row.target)).length

          }))
          .reduce((prevVal, curr) => prevVal
            .concat({name: curr.name, value: curr.firsts, color: colors.first})
            .concat({name: curr.name, value: curr.thirds, color: colors.third}), []);
          $scope.$apply();
      });
    };

  }])
  .directive('bar', function($compile) {
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

          var m = [30, 10, 10, 150],
            w = 960 - m[1] - m[3],
            h = 930 - m[0] - m[2];

          var format = d3.format(",.0f");

          var x = d3.scale.linear().range([0, w]),
            y = d3.scale.ordinal().rangeRoundBands([0, h], .1);

          var xAxis = d3.svg.axis().scale(x).orient("top").tickSize(-h),
            yAxis = d3.svg.axis().scale(y).orient("left").tickSize(0);

          var description = d3.select(element[0])
            .append("div")
            .text(
            "False first parties as returned by lightbeam are marked with lightblue color. " +
            "Third parties loaded are marked with green color.");

          var svg = d3.select(element[0])
            .append("svg")
            .attr("class", "bar-style")
            .attr("width", w + m[1] + m[3])
            .attr("height", h + m[0] + m[2])
            .append("g")
            .attr("transform", "translate(" + m[3] + "," + m[0] + ")");

            // Parse numbers, and sort by value.
            scope.data.forEach(function (d) {
              d.value = +d.value;
            });
          scope.data.sort(function (a, b) {
              return b.value - a.value;
            });

            // Set the scale domain.
            x.domain([0, d3.max(scope.data, function (d) {
              return d.value;
            })]);
            y.domain(scope.data.map(function (d) {
              return d.name;
            }));

            var bar = svg.selectAll("g.bar")
              .data(scope.data)
              .enter().append("g")
              .attr("class", "bar")
              .attr("transform", function (d) {
                return "translate(0," + y(d.name) + ")";
              });

            bar.append("rect")
              .attr("width", function (d) {
                return x(d.value);
              })
              .attr("style", (d) => d.color ? ("fill: " + d.color) : "")
              .attr("height", y.rangeBand());

            bar.append("text")
              .attr("class", "value")
              .attr("x", function (d) {
                return x(d.value);
              })
              .attr("y", y.rangeBand() / 2)
              .attr("dx", -3)
              .attr("dy", ".35em")
              .attr("text-anchor", "end")
              .text(function (d) {
                return format(d.value);
              });

            svg.append("g")
              .attr("class", "x axis")
              .call(xAxis);

            svg.append("g")
              .attr("class", "y axis")
              .call(yAxis);

        });
      }
    };
  })
  .name;
