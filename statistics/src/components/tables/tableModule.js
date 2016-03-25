/**
 * Created by alexandros on 3/17/16.
 */

import angular from 'angular';
import Utilities from '../../Utilities';
require('angular-ui-bootstrap');

export default angular
  .module('table', ['ui.bootstrap'])
  .controller('TableController', ['$scope', function($scope) {
    $scope.data = null;
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.TABLE,
      (loaded) => {if (loaded && $scope.data === null) fetchData();});
    const fetchData = function() {
      console.log('fetch em');
      $scope.connection.distinct()
        .then((rows) => $scope.stats = [
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
        ]
      );
    };

  }])
  .directive('myTable', function($compile) {
    return {
      template: require('./table.html'),
      link: function(scope, element, attrs) {
        setTimeout(() => $compile(element.contents())(scope), 1000);
      }
    };
  })
  .name;
