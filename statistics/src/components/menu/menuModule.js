/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
import {Utilities} from 'adblocker-utils';
import jQuery from 'jquery';
require('angular-ui-bootstrap');
import moment from 'moment';

import tableModule from '../../components/tables/tableModule';
import calculationTableModule from '../../components/tables/calculationTableModule';
import edgeBundlingModule from '../../components/charts/edgeBundlingModule';
import barModule from '../../components/charts/barModule';
import forceDirectedModule from '../../components/charts/forceDirectedModule';
import stackedBarModule from '../../components/charts/stackedBarModule';
import lineChartModule from '../../components/charts/lineChartModule';
import {DbConnection} from 'adblocker-utils';
import GraphStats from '../../GraphStats';
import jStat from 'jStat';

Utilities.constants.instances = {
  GHOSTERY_DEFAULT: 'data_Ghostery_Default',
  ADBLOCKPLUS_DEFAULT: 'data_Adblockplus_Default',
  GHOSTERY_MAXPROTECTION: 'data_Ghostery_MaxProtection',
  ADBLOCKPLUS_MAXPROTECTION: 'data_Adblockplus_MaxProtection',
  NOADBLOCKER: 'data_NoAdblocker',
  NOADBLOCKER_DNT: 'data_NoAdblocker_DNT'
};
// TODO
Utilities.constants.menuItems = {
  TABLE: 'table',
  CALCULATED_DATES: 'calculated-dates',
  FIRST_MEANS: 'first-means',
  THIRD_MEANS: 'third-means',
  FIRST_STDEV: 'first-stdev',
  THIRD_STDEV: 'third-stdev',
  //DIAMETER: 'diameter',
  DENSITY: 'density',
  //BETWEENNESS_CENTRALITY: 'betweenness-centrality'
};

export default angular
  .module('menu', ['ui.bootstrap',
    tableModule,
    edgeBundlingModule,
    barModule,
    stackedBarModule,
    lineChartModule,
    calculationTableModule,
  forceDirectedModule])
  .service('statsCalculator', function() {

  })
  .controller('DataController', ['$scope', function($scope) {

    $scope.calculatedDates = {};
      //$scope.minDate = moment().

    $scope.connection._find($scope.connection._statsTable)
      .then(data => {
        $scope.stats = data;
        const minDate = data
          .map(o => new Date(o.crawlDate))
          .reduce((min, curr) => Math.min(min, curr), new Date('04/15/2016'));
        const calculatedDates = data
          .map(o => ({
            date: moment(new Date(o.crawlDate)).format(Utilities.constants.DATE_FORMAT),
            instance: o.instance
          }))
          .reduce((cum, curr) => {
            cum[curr.date] = jQuery.extend({}, cum[curr.date]);
            cum[curr.date][curr.instance] = true;
            return cum;
          }, {});

        for (
          let date = moment(minDate);
          !date.isAfter(new Date(), 'days');
          date = date.add(1, 'days')) {

          if (!(date.format(Utilities.constants.DATE_FORMAT) in calculatedDates)) {
            calculatedDates[date.format(Utilities.constants.DATE_FORMAT)] = false;
          }
        }
        $scope.calculatedDates = Object.keys(calculatedDates)
          .map(date => ({date: date, isCalculated: calculatedDates[date]}))
          .sort((d1, d2) => new Date(d1.date) - new Date(d2.date));
        $scope.$apply();
      });
  }])
  .controller('MenuController', ['$scope', function($scope) {


    $scope.activate = (menuItem) => $scope.selected = menuItem;



    //$scope.$watch(scope => scope.selected, selected => console.log('selected: ' + selected));

    $scope.instance = Utilities.constants.instances.GHOSTERY_DEFAULT;
    $scope.date = new Date();
    $scope.graphStats = {};
    $scope.menuItems = Utilities.constants.menuItems;
    $scope.selected = Utilities.constants.menuItems.CALCULATED_DATES;

    $scope.select = function(menuItem) {
      $scope.selected = menuItem;
      //$scope.$apply();
    };
    $scope.setDate = function(date) {
      $scope.date = new Date(date);
    };

    $scope.view = function(date, instance) {
      $scope.date = date;
      $scope.selected = Utilities.constants.menuItems.TABLE;
      $scope.instance = instance;
    }

    //$scope.$watch(scope => scope.date || scope.instance,
    //    date => $scope.connection
    //    ._find($scope.instance, {crawlDate: moment($scope.date).format(Utilities.constants.DATE_FORMAT)})
    //    .then(function(data) {
    //        $scope.graphStats[$scope.instance] = new GraphStats(data);
    //        $scope.currentGraphStats = $scope.graphStats[$scope.instance];
    //        $scope.$apply();
    //    })
    //);
  }])
  .directive('myMenu', function() {

    return {
      template: require('./menu.html')
    };
  })
  .name;
