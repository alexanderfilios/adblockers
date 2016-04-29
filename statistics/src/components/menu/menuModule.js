/**
 * Created by alexandros on 3/18/16.
 */

import angular from 'angular';
import {Utilities} from 'adblocker-utils';
import jQuery from 'jquery';
require('angular-ui-bootstrap');
import moment from 'moment';
import JSZip from 'jszip';

import tableModule from '../../components/tables/tableModule';
import calculationTableModule from '../../components/tables/calculationTableModule';
import edgeBundlingModule from '../../components/charts/edgeBundlingModule';
import barModule from '../../components/charts/barModule';
import forceDirectedModule from '../../components/charts/forceDirectedModule';
import stackedBarModule from '../../components/charts/stackedBarModule';
import lineChartModule from '../../components/charts/lineChartModule';
import mapModule from '../../components/charts/mapModule';
import {DbConnection} from 'adblocker-utils';
import GraphStats from '../../GraphStats';
import jStat from 'jStat';

Utilities.constants.instances = {
  GHOSTERY_DEFAULT: 'data_Ghostery_Default',
  ADBLOCKPLUS_DEFAULT: 'data_Adblockplus_Default',
  GHOSTERY_MAXPROTECTION: 'data_Ghostery_MaxProtection',
  ADBLOCKPLUS_MAXPROTECTION: 'data_Adblockplus_MaxProtection',
  NOADBLOCKER: 'data_NoAdblocker',
  NOADBLOCKER_DNT: 'data_NoAdblocker_DNT',

  GHOSTERY_DEFAULT_MUA: 'data_Ghostery_Default_MUA',
  ADBLOCKPLUS_DEFAULT_MUA: 'data_Adblockplus_Default_MUA',
  GHOSTERY_MAXPROTECTION_MUA: 'data_Ghostery_MaxProtection_MUA',
  ADBLOCKPLUS_MAXPROTECTION_MUA: 'data_Adblockplus_MaxProtection_MUA',
  NOADBLOCKER_MUA: 'data_NoAdblocker_MUA',
  NOADBLOCKER_DNT_MUA: 'data_NoAdblocker_DNT_MUA'
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
  MAP: 'map',
  UNRECOGNIZED_THIRD_PARTY_REQUESTS: 'unrecognized',
  MISCLASSIFIED_REQUESTS: 'misclassified'
  //BETWEENNESS_CENTRALITY: 'betweenness-centrality'
};

export default angular
  .module('menu', ['ui.bootstrap',
    tableModule,
    edgeBundlingModule,
    barModule,
    stackedBarModule,
    lineChartModule,
    mapModule,
    calculationTableModule,
  forceDirectedModule])
  .service('csvService', function() {
    const self = this;
    self.getCsvDataInRange = function(dataDict, dateRange) {
      const dates = [];
      for (let d = moment(dateRange.min);
           !d.isAfter(dateRange.max);
           d = d.add(1, 'days')) {
        dates.push(d.format(Utilities.constants.DATE_FORMAT));
      }
      const instances = Object.values(Utilities.constants.instances);

      return 'Date,' + instances.join(',') + '\n'
      + dates.map(date =>
          date + ',' + instances
            .map(instance => dataDict[date] && dataDict[date][instance] && dataDict[date][instance].value || '')
            .join(',')
      ).join('\n');
      //Object.values(Utilities.constants.instances)
      //  .map(v => v.)
    };
    self.getAllCsvData = function(data) {
      const dateRange = data
        .map(d => new Date(d.crawlDate))
        .reduce((cum, cur) => ({
          min: Math.min(cum.min, cur),
          max: Math.max(cum.max, cur)
        }), {min: new Date(), max: new Date(0)});

      const dataDict = data
        .reduce((cum, cur) => {
          if (!(cur.name in cum)) {
            cum[cur.name] = {};
          }
          if (!(cur.crawlDate in cum[cur.name])) {
            cum[cur.name][cur.crawlDate] = {};
          }
          cum[cur.name][cur.crawlDate][cur.instance] = cur;
          return cum;
        }, {});

      return Object.keys(dataDict)
        .reduce((cum, metric) => {
          cum.file(metric + '.csv', self.getCsvDataInRange(dataDict[metric], dateRange))
          return cum;
        }, new JSZip())
        .generateAsync({type: 'blob'});
    };
  })
  .controller('DataController', ['$scope', 'csvService', function($scope, csvService) {

    $scope.filename = 'graphs.zip';
    $scope.csvData = null;
    $scope.calculatedDates = {};
    $scope.connection._find($scope.connection._statsTable)
      .then(data => {
        $scope.stats = data;
        return csvService.getAllCsvData(data);
      })
      .then(csvData => {
        $scope.csvData = csvData;

        const minDate = $scope.stats
          .map(o => new Date(o.crawlDate))
          .reduce((min, curr) => Math.min(min, curr), new Date('04/15/2016'));
        const calculatedDates = $scope.stats
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
    $scope.select = (menuItem) => $scope.selected = menuItem;
    $scope.view = function(date, instance) {
      $scope.date = date;
      $scope.selected = Utilities.constants.menuItems.TABLE;
      $scope.instance = instance;
      $scope.connection._find(instance, {crawlDate: date})
        .then(data => {
          $scope.graphStats = new GraphStats(data);
          $scope.$apply();
        });

    };
    $scope.instance = null;//Utilities.constants.instances.GHOSTERY_DEFAULT;
    $scope.date = new Date();
    $scope.graphStats = {};
    $scope.menuItems = Utilities.constants.menuItems;
    $scope.selected = Utilities.constants.menuItems.CALCULATED_DATES;

  }])
  .filter('formatDate', function() {
    return (date) => moment(date).format(Utilities.constants.DATE_FORMAT);
  })
  .directive('myMenu', function() {

    return {
      template: require('./menu.html'),
      link: function (scope, element, attrs) {
        scope.$watch('csvData', function() {
          if (!!scope.csvData) {
            const button = jQuery(element).find('#download-csv')
              .attr('class', 'btn btn-default')
              .attr('href', window.URL.createObjectURL(scope.csvData))
              .attr('download', scope.filename);
          }
        });
      }
    };
  })
  .name;
