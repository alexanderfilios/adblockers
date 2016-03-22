/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
import Constants from '../../Constants';
require('angular-ui-bootstrap');

import tableModule from '../../components/tables/tableModule';
import edgeBundlingModule from '../../components/charts/edgeBundlingModule';
import barModule from '../../components/charts/barModule';
import forceDirectedModule from '../../components/charts/forceDirectedModule';

export default angular
  .module('menu', ['ui.bootstrap',
    tableModule,
    edgeBundlingModule,
    barModule,
  forceDirectedModule])
  .controller('MenuController', ['$scope', function($scope) {

    $scope.activate = function(menuItem) {
      $scope.selected = menuItem;
    };

    //setTimeout(() => $scope.$broadcast('someEvent'), 3000);


    $scope.menuItems = Constants.menuItems;
    $scope.selected = Constants.menuItems.BAR;
  }])
  .directive('myMenu', function() {

    return {
      template: require('./menu.html')
    };
  })
  .name;
