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
import GraphStats from '../../GraphStats';


export default angular
  .module('menu', ['ui.bootstrap',
    tableModule,
    edgeBundlingModule,
    barModule,
    stackedBarModule,
    lineChartModule,
  forceDirectedModule])
  .service('graphGetter', function() {

  })
  .controller('MenuController', ['$scope', function($scope) {


    $scope.activate = (menuItem) => $scope.selected = menuItem;
    Utilities.constants.instances = {
      GHOSTERY_DEFAULT: 'data_Ghostery_Default'
    };
    $scope.instance = Utilities.constants.instances.GHOSTERY_DEFAULT;
    $scope.date = new Date();
    $scope.graphStats = {};
    $scope.menuItems = Utilities.constants.menuItems;
    $scope.selected = Utilities.constants.menuItems.LINE_CHART;

    $scope.$watch(scope => scope.date || scope.instance,
        date => $scope.connection
        ._find($scope.instance, {crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
        .then(function(data) {
          //$scope.graphStats = new GraphStats(data);

          $scope.graphStats[$scope.instance] = new GraphStats(data);
            $scope.currentGraphStats = $scope.graphStats[$scope.instance];
            $scope.$apply();
        })
    );
  }])
  .directive('myMenu', function() {

    return {
      template: require('./menu.html')
    };
  })
  .name;
