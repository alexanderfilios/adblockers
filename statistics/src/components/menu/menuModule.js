/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
import {Utilities} from 'adblocker-utils';
import jQuery from 'jquery';
require('angular-ui-bootstrap');
import moment from 'moment';

import tableModule from '../../components/tables/tableModule';
import edgeBundlingModule from '../../components/charts/edgeBundlingModule';
import barModule from '../../components/charts/barModule';
import forceDirectedModule from '../../components/charts/forceDirectedModule';
import stackedBarModule from '../../components/charts/stackedBarModule';
import lineChartModule from '../../components/charts/lineChartModule';
import {DbConnection} from 'adblocker-utils';


export default angular
  .module('menu', ['ui.bootstrap',
    tableModule,
    edgeBundlingModule,
    barModule,
    stackedBarModule,
    lineChartModule,
  forceDirectedModule])
  .controller('MenuController', ['$scope', function($scope) {

    $scope.activate = function(menuItem) {
      $scope.selected = menuItem;
    };

    $scope.date = new Date();
    $scope.menuItems = Utilities.constants.menuItems;
    $scope.selected = Utilities.constants.menuItems.LINE_CHART;
  }])
  .directive('myMenu', function() {

    return {
      template: require('./menu.html')
    };
  })
  .name;
