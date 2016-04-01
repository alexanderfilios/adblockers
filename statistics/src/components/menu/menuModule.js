/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
import {Utilities} from 'adblocker-utils';
require('angular-ui-bootstrap');

import tableModule from '../../components/tables/tableModule';
import edgeBundlingModule from '../../components/charts/edgeBundlingModule';
import barModule from '../../components/charts/barModule';
import forceDirectedModule from '../../components/charts/forceDirectedModule';
import stackedBarModule from '../../components/charts/stackedBarModule';
import {DbConnection} from 'adblocker-utils';
console.log('lets see that one');
console.log(DbConnection);
//console.log('thats the consts');
//console.log(JSON.stringify(Constants));

export default angular
  .module('menu', ['ui.bootstrap',
    tableModule,
    edgeBundlingModule,
    barModule,
    stackedBarModule,
  forceDirectedModule])
  .controller('MenuController', ['$scope', function($scope) {

    $scope.activate = function(menuItem) {
      $scope.selected = menuItem;
    };

    $scope.date = new Date();
    $scope.menuItems = Utilities.constants.menuItems;
    $scope.selected = Utilities.constants.menuItems.TABLE;
  }])
  .directive('myMenu', function() {

    return {
      template: require('./menu.html')
    };
  })
  .name;
