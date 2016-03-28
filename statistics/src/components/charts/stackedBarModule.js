/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');

const d3 = require('d3');
import Utilities from '../../Utilities';

export default angular
  .module('stackedBar', ['ui.bootstrap'])
  .controller('StackedBarController', ['$scope', function($scope) {

    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.STACKED_BAR,
      (loaded) => {if (loaded && $scope.data === null) fetchData();});
    const fetchData = function() {

      const categories = ['image', 'application', 'text', 'font', 'video'];
      let bars = [];

      Utilities.executeSerially(
        [
          {database: 'myapp_test1', title: 'DB 1'},
          {database: 'myapp_test1', title: 'DB 2'},
          {database: 'myapp_test1', title: 'DB 3'},
          {database: 'myapp_test1', title: 'DB 4'},
          {database: 'myapp_test2', title: 'DB 5'},
          {database: 'myapp_test3', title: 'DB 6'}
        ],
        (input, output) => bars = bars.concat([{barTitle: input.title, data: output}]),
        (bar) => $scope.connection.find({}, bar.database)
      ).then(() => $scope.data = {
          units: 'Requests',
          bars: bars.map(bar => categories.reduce(function(accumulator, current) {
            accumulator[current] = bar.data.filter(row => row.contentType.split('/')[0] === current).length;
            return accumulator;
          }, {barTitle: bar.barTitle}))
        });
    };

  }])
  .directive('stackedBar', function($compile) {
    return {
      link: function(scope, element, attrs) {

        var margin = {top: 20, right: 20, bottom: 30, left: 40},
          width = 960 - margin.left - margin.right,
          height = 500 - margin.top - margin.bottom;

        var x = d3.scale.ordinal()
          .rangeRoundBands([0, width], .1);

        var y = d3.scale.linear()
          .rangeRound([height, 0]);


        var xAxis = d3.svg.axis()
          .scale(x)
          .orient("bottom");

        var yAxis = d3.svg.axis()
          .scale(y)
          .orient("left")
          .tickFormat(d3.format(".2s"));

        var svg = d3.select(element[0])
          .append("svg")
          .attr("class", "stacked-bar-style")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")");


        if (typeof scope.data === 'string') {
          d3.csv(scope.data, function(error, data) {
            if (error) throw error;

            //data.units = 'Requests';
            //data.bars = [
            //  {
            //    barTitle: 'A',
            //    a: 10,
            //    b: 12,
            //    c: 14
            //  },
            //  {
            //    barTitle: 'B',
            //    a: 10,
            //    b: 10,
            //    c: 10
            //  },
            //  {
            //    barTitle: 'C',
            //    a: 10,
            //    b: 10,
            //    c: 10
            //  },
            //  {
            //    barTitle: 'D',
            //    a: 10,
            //    b: 10,
            //    c: 10
            //  }
            //];
            createBars(data);
          });
        } else {
          Utilities
            .repeatPromise(null, () => scope.data !== null, 500)
            .then(() => createBars(scope.data), 2000);
        }


        function createBars(data) {

          var color = d3.scale.ordinal()
            .range(["#98abc5", "#8a89a6", "#7b6888", "#6b486b", "#a05d56", "#d0743c", "#ff8c00"]);

          color.domain(d3.keys(data.bars[0]).filter(function(key) { return key !== "barTitle"; }));
          data.bars.forEach(function(d) {
            var y0 = 0;
            d.barParts = color.domain().map(function(name) { return {name: name, y0: y0, y1: y0 += +d[name]}; });
            d.total = d.barParts[d.barParts.length - 1].y1;
          });

          data.bars.sort(function(a, b) { return b.total - a.total; });

          x.domain(data.bars.map(function(d) { return d.barTitle; }));
          y.domain([0, d3.max(data.bars, function(d) { return d.total; })]);

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

          var state = svg.selectAll(".state")
            .data(data.bars)
            .enter().append("g")
            .attr("class", "g")
            .attr("transform", function(d) { return "translate(" + x(d.barTitle) + ",0)"; });

          state.selectAll("rect")
            .data(function(d) { return d.barParts; })
            .enter().append("rect")
            .attr("width", x.rangeBand())
            .attr("y", function(d) { return y(d.y1); })
            .attr("height", function(d) { return y(d.y0) - y(d.y1); })
            .style("fill", function(d) { return color(d.name); });

          var legend = svg.selectAll(".legend")
            .data(color.domain().slice().reverse())
            .enter().append("g")
            .attr("class", "legend")
            .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

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
            .text(function(d) { return d; });
        }

      }
    };
  })
  .name;
