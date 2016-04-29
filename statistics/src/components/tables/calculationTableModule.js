/**
 * Created by alexandros on 4/23/16.
 */

import angular from 'angular';
import {Utilities} from 'adblocker-utils';
import moment from 'moment';
import GraphStats from '../../GraphStats';

export default angular
  .module('calculationTable', ['ui.bootstrap'])
  .service('calculationTableService', function() {

    this.calculate = function(data, redirectionMappingData, date, instance) {
      if (!!redirectionMappingData && Array.isArray(redirectionMappingData)) {
        data = GraphStats.replaceRedirections(data, redirectionMappingData);
      }
      const graphStats = new GraphStats(data);

      return [
        {
          name: Utilities.constants.menuItems.FIRST_MEANS,
          value: graphStats.getMeanDegree(true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.FIRST_STDEV,
          value: graphStats.getStdevDegree(true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.THIRD_MEANS,
          value: graphStats.getMeanDegree(false),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.THIRD_STDEV,
          value: graphStats.getStdevDegree(false),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.DENSITY,
          value: graphStats.getDensity(),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.MISCLASSIFIED_REQUESTS,
          value: graphStats.getMisclassifiedRequests(),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.UNRECOGNIZED_THIRD_PARTY_REQUESTS,
          value: graphStats.getUnrecognizedThirdPartyRequests(),
          instance: instance,
          crawlDate: date
        }
        //{
        //  name: Utilities.constants.menuItems.BETWEENNESS_CENTRALITY,
        //  value: graphStats.getMeanBetweennessCentrality(),
        //  instance: instance,
        //  crawlDate: date
        //},
        //{
        //  name: Utilities.constants.menuItems.DIAMETER,
        //  value: graphStats.getDiameter(),
        //  instance: instance,
        //  crawlDate: date
        //}
      ];
    };
  })
  .controller('CalculationTableController', ['$scope', 'calculationTableService', function($scope, calculationTableService) {
    $scope.instances = Utilities.constants.instances;
    $scope.connection._insertMultiple = function (data, collection) {
      return data.reduce((cum, curr) => cum.then(() =>
          $scope.connection._insert(curr, collection)
      ), Promise.resolve());
    };
    $scope.calculate = function (date, instance) {
      console.log('Calculating stats for instance ' + instance + '...');
      const start = moment();
      let requestData = [];
      $scope.connection._find(instance,
        {crawlDate: moment(new Date(date)).format(Utilities.constants.DATE_FORMAT)})
        //)
        .then(data => {
          requestData = data;
          return $scope.connection._find($scope.connection._redirectionMappingTable);
        })
        .then(redirectionMappingData => Promise.resolve(calculationTableService.calculate(requestData, redirectionMappingData, date, instance)))
        .then((data) => $scope.connection._insertMultiple(data, $scope.connection._statsTable))
        .then((data) => $scope.connection._find($scope.connection._statsTable))
        .then((data) => alert('Completed stats calculation of instance ' + instance + ' for ' + date + ' in ' + moment().diff(start, 'seconds') + ' seconds. Reload the page to see the results!'));
    };
    $scope.clear = function (date, instance) {
      $scope.connection._find($scope.connection._statsTable, {crawlDate: date, instance: instance})
        .then((data) => data
          // For double security, refilter the data
          .filter(d => d.crawlDate === date)
          .filter(d => d.instance === instance)
          .forEach(d => $scope.connection._delete($scope.connection._statsTable, d._id)));
    };
  }])
  .directive('calculationTable', function($compile) {
    return {
      template: require('./calculationTable.html')
    };
  })
  .name;
