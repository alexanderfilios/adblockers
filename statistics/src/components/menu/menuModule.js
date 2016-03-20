/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');

import tableModule from '../../components/tables/tableModule';
import edgeBundlingModule from '../../components/charts/edgeBundlingModule';
import barModule from '../../components/charts/barModule';

export default angular
  .module('menu', ['ui.bootstrap',
    tableModule,
    edgeBundlingModule,
    barModule])
  .controller('MenuController', ['$scope', function($scope) {

    const menuItems = {
      BUNDLE: 1,
      TABLE: 2,
      BAR: 3
    };

    $scope.menuItems = menuItems;
    $scope.selected = menuItems.BAR;
  }])
  .directive('myMenu', function() {

    return {
      template: require('./menu.html')
    };
  })
  .name;
