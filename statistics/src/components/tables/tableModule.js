/**
 * Created by alexandros on 3/17/16.
 */

import angular from 'angular';
import Utilities from '../../Utilities';
require('angular-ui-bootstrap');

export default angular
  .module('table', ['ui.bootstrap'])
  .controller('TableController', ['$scope', function($scope) {
    $scope.name = 'My name';
    $scope.$watch(
      (scope) => scope.selected === Utilities.constants.menuItems.TABLE,
      (loaded) => {if (loaded && $scope.data === null) fetchData();});
    const fetchData = function() {
      $scope.connection.distinct().then((rows) => $scope.rows = rows.slice(0,1));
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
