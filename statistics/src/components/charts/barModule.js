/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');

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
    $scope.connection.find().then(data => {
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
    });
  }])
  .directive('bar', function($compile) {
    return {
      link: function(scope, element, attrs) {

        var m = [30, 10, 10, 150],
          w = 960 - m[1] - m[3],
          h = 930 - m[0] - m[2];

        var format = d3.format(",.0f");

        var x = d3.scale.linear().range([0, w]),
          y = d3.scale.ordinal().rangeRoundBands([0, h], .1);

        var xAxis = d3.svg.axis().scale(x).orient("top").tickSize(-h),
          yAxis = d3.svg.axis().scale(y).orient("left").tickSize(0);

        var svg = d3.select(element[0])
          .append("svg")
          .attr("class", "bar-style")
          .attr("width", w + m[1] + m[3])
          .attr("height", h + m[0] + m[2])
          .append("g")
          .attr("transform", "translate(" + m[3] + "," + m[0] + ")");

        if (typeof scope.data === 'string') {
          d3.csv(scope.data, function(error, data) {
            if (error) throw error;
            console.log(data);
            createBars(data);
          });
        } else {
          Utilities
            .repeatPromise(null, () => scope.data !== null, 500)
            .then(() => createBars(scope.data), 2000);
        }


        function createBars(data) {
          // Parse numbers, and sort by value.
          data.forEach(function(d) { d.value = +d.value; });
          data.sort(function(a, b) { return b.value - a.value; });

          // Set the scale domain.
          x.domain([0, d3.max(data, function(d) { return d.value; })]);
          y.domain(data.map(function(d) { return d.name; }));

          var bar = svg.selectAll("g.bar")
            .data(data)
            .enter().append("g")
            .attr("class", "bar")
            .attr("transform", function(d) { return "translate(0," + y(d.name) + ")"; });

          bar.append("rect")
            .attr("width", function(d) { return x(d.value); })
            .attr("style", (d) => d.color ? ("fill: " + d.color) : "")
            .attr("height", y.rangeBand());

          bar.append("text")
            .attr("class", "value")
            .attr("x", function(d) { return x(d.value); })
            .attr("y", y.rangeBand() / 2)
            .attr("dx", -3)
            .attr("dy", ".35em")
            .attr("text-anchor", "end")
            .text(function(d) { return format(d.value); });

          svg.append("g")
            .attr("class", "x axis")
            .call(xAxis);

          svg.append("g")
            .attr("class", "y axis")
            .call(yAxis);
        }

      }
    };
  })
  .name;
