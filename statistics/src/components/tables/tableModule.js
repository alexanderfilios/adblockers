/**
 * Created by alexandros on 3/17/16.
 */

import angular from 'angular';
import {Utilities} from 'adblocker-utils';
import moment from 'moment';
require('angular-ui-bootstrap');

export default angular
  .module('table', ['ui.bootstrap'])
  .service('tableService', function() {
    this.getStats = function(stats, date, instance) {
      if (!stats || !date) {
        return [];
      }
      return [
        {
          description: 'Date',
          value: date
        },
        {
          description: 'Instance',
          value: instance
        },
        {
          description: 'First means',
          value: stats
            .filter(s => s.crawlDate === moment(new Date(date)).format(Utilities.constants.DATE_FORMAT))
            .filter(s => s.name === Utilities.constants.menuItems.FIRST_MEANS)
            .filter(s => s.instance === instance)
            .map(s => s.value)
        },
        {
          description: 'Third means',
          value: stats
            .filter(s => s.crawlDate === moment(new Date(date)).format(Utilities.constants.DATE_FORMAT))
            .filter(s => s.name === Utilities.constants.menuItems.THIRD_MEANS)
            .filter(s => s.instance === instance)
            .map(s => s.value)
        },
        {
          description: 'First Stdev',
          value: stats
            .filter(s => s.crawlDate === moment(new Date(date)).format(Utilities.constants.DATE_FORMAT))
            .filter(s => s.name === Utilities.constants.menuItems.FIRST_STDEV)
            .filter(s => s.instance === instance)
            .map(s => s.value)
        },
        {
          description: 'Third Stdev',
          value: stats
            .filter(s => s.crawlDate === moment(new Date(date)).format(Utilities.constants.DATE_FORMAT))
            .filter(s => s.name === Utilities.constants.menuItems.THIRD_STDEV)
            .filter(s => s.instance === instance)
            .map(s => s.value)
        },
        {
          description: 'Density',
          value: stats
            .filter(s => s.crawlDate === moment(new Date(date)).format(Utilities.constants.DATE_FORMAT))
            .filter(s => s.name === Utilities.constants.menuItems.DENSITY)
            .filter(s => s.instance === instance)
            .map(s => s.value)
        }
      ]
    }
    this.getTableData = function(graphStats) {

      if (graphStats === undefined) {
        return null;
      }

      return [
        {
          description: 'Total requests',
          value: graphStats.data.length
        },
        {
          description: 'Total third-party requests',
          value: graphStats.data.filter(r => Utilities.isTp(r)).length
        },
        {
          description: 'Total first-party requests (US)',
          value: graphStats.data.filter(r => Utilities._urisMatch(r.firstParty, r.target)).length
        },
        {
          description: 'Total third-party requests (LB)',
          value: graphStats.data.filter(r => !Utilities._urisMatch(r.source, r.target)).length
        },
        {
          description: 'Total first-party requests (LB)',
          value: graphStats.data.filter(r => Utilities._urisMatch(r.source, r.target)).length
        },
        {
          description: 'Total first parties (US)',
          value: Array.from(graphStats.graph.nodes(true)).filter(n => n[1].f).length
        },
        {
          description: 'Total first parties (LB)',
          value: jQuery.unique(graphStats.data.map(r => r.source)).length
        },
        {
          description: 'Total third parties (US & LB)',
          value: Array.from(graphStats.graph.nodes(true)).filter(n => !n[1].f).length
        },
        {
          description: 'Density',
          value: graphStats.getDensity()
        },
        {
          description: 'Diameter',
          value: graphStats.getDiameter()
        },
        {
          description: 'Mean betweenness centrality',
          value: graphStats.getMeanBetweennessCentrality()
        },
        {
          description: 'Mean node degree',
          value: graphStats.getMeanDegree()
        },
        {
          description: 'Node degree stdev',
          value: graphStats.getStdevDegree()
        }
      ]
    };

  })
  .controller('TableController', ['$scope', 'tableService', function($scope, tableService) {
    $scope.data = null;

    $scope.$watch(
        scope => scope.date,
        date => $scope.rows = tableService.getStats($scope.stats, $scope.date, $scope.instance));
    //$scope.$watch(
    //  scope => scope.date && scope.instance
    //)
    //$scope.$watch(
    //    scope => scope.currentGraphStats,
    //    graphStats => $scope.stats = tableService.getTableData(graphStats)
    //);
    //$scope.filter = {
    //  date: $scope.date
    //};

    //$scope.connection.find({crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
    //  .then(rows => {
    //    console.log('FALSE FIRST PARTY REQUESTS:');
    //    console.log(rows.filter(row => Utilities.isFalseFp(row)));
    //  });

    //$scope.data = null;
    //$scope.$watch(
    //  (scope) => scope.selected === Utilities.constants.menuItems.TABLE && scope.date,
    //  (loaded) => {if (loaded && $scope.data === null) fetchData($scope.date);});
    //const fetchData = function(date) {
    //  $scope.connection.distinct({crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
    //    .then((rows) => {$scope.stats = [
    //      {
    //        description: 'Total requests',
    //        value: rows.length
    //      },
    //      {
    //        description: 'Total third-party requests (US)',
    //        value: rows.filter(r => Utilities.isTp(r)).length
    //      },
    //      {
    //        description: 'Total first-party requests (US)',
    //        value: rows.filter(r => Utilities._urisMatch(r.firstParty, r.target)).length
    //      },
    //      {
    //        description: 'Total third-party requests (LB)',
    //        value: rows.filter(r => !Utilities._urisMatch(r.source, r.target)).length
    //      },
    //      {
    //        description: 'Total first-party requests (LB)',
    //        value: rows.filter(r => Utilities._urisMatch(r.source, r.target)).length
    //      },
    //      {
    //        description: 'Total first parties (US)',
    //        value: jQuery.unique(rows.map(r => r.firstParty)).length
    //      },
    //      {
    //        description: 'Total first parties (LB)',
    //        value: jQuery.unique(rows.map(r => r.source)).length
    //      },
    //      {
    //        description: 'Total third parties (US & LB)',
    //        value: jQuery.unique(rows.map(r => r.target)).length
    //      }
    //    ]; $scope.$apply();}
    //  );
    //};

  }])
  .directive('myTable', function($compile) {
    return {
      template: require('./table.html')
    };
  })
  .name;
