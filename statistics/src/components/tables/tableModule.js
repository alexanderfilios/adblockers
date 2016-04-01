/**
 * Created by alexandros on 3/17/16.
 */

import angular from 'angular';
import {Utilities} from 'adblocker-utils';
import moment from 'moment';
require('angular-ui-bootstrap');

export default angular
  .module('table', ['ui.bootstrap'])
  .controller('TableController', ['$scope', function($scope) {
    $scope.filter = {
      date: $scope.date
    };

    $scope.connection.find({crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
      .then(rows => {
        console.log('FALSE FIRST PARTY REQUESTS:');
        console.log(rows.filter(row => Utilities.isFalseFp(row)));
      });

    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.TABLE && scope.date,
      (loaded) => {if (loaded && $scope.data === null) fetchData($scope.date);});
    const fetchData = function(date) {
      $scope.connection.distinct({crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
        .then((rows) => {$scope.stats = [
          {
            description: 'Total requests',
            value: rows.length
          },
          {
            description: 'Total third-party requests (US)',
            value: rows.filter(r => Utilities.isTp(r)).length
          },
          {
            description: 'Total first-party requests (US)',
            value: rows.filter(r => Utilities._urisMatch(r.firstParty, r.target)).length
          },
          {
            description: 'Total third-party requests (LB)',
            value: rows.filter(r => !Utilities._urisMatch(r.source, r.target)).length
          },
          {
            description: 'Total first-party requests (LB)',
            value: rows.filter(r => Utilities._urisMatch(r.source, r.target)).length
          },
          {
            description: 'Total first parties (US)',
            value: jQuery.unique(rows.map(r => r.firstParty)).length
          },
          {
            description: 'Total first parties (LB)',
            value: jQuery.unique(rows.map(r => r.source)).length
          },
          {
            description: 'Total third parties (US & LB)',
            value: jQuery.unique(rows.map(r => r.target)).length
          }
        ]; $scope.$apply();}
      );
    };

  }])
  .directive('myTable', function($compile) {
    return {
      template: require('./table.html')
    };
  })
  .name;
