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

    this.calculate = function(data, date, instance, entityDetails, firstPartyData, redirectionMappingData) {
      const graphStats = new GraphStats(data, entityDetails);
      return [
        {
          name: Utilities.constants.menuItems.TOP_1000_FIRST_DEGREE,
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
          name: Utilities.constants.menuItems.TOP_1000_THIRD_DEGREE,
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
        },
        {
          name: Utilities.constants.menuItems.TOP_10_FIRST_DEGREE,
          value: graphStats.getTopMeanDegree(true, 10),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_10_THIRD_DEGREE,
          value: graphStats.getTopMeanDegree(false, 10),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_1_FIRST_DEGREE,
          value: graphStats.getTopMeanDegree(true, 1),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_1_THIRD_DEGREE,
          value: graphStats.getTopMeanDegree(false, 1),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_1_FIRST_DEGREE_ENTITIES,
          value: graphStats.getTopMeanDegree(true, 1, true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_10_FIRST_DEGREE_ENTITIES,
          value: graphStats.getTopMeanDegree(true, 10, true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_1000_FIRST_DEGREE_ENTITIES,
          value: graphStats.getMeanDegree(true, true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_1_THIRD_DEGREE_ENTITIES,
          value: graphStats.getTopMeanDegree(false, 1, true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_10_THIRD_DEGREE_ENTITIES,
          value: graphStats.getTopMeanDegree(false, 10, true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_1000_THIRD_DEGREE_ENTITIES,
          value: graphStats.getMeanDegree(false, true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.DENSITY_ENTITIES,
          value: graphStats.getDensity(true),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.TOP_500_FIRST_DEGREE,
          value: graphStats.getMeanDegreeOfNodes(redirectionMappingData, firstPartyData, (d => d.rank <= 500)),
          instance: instance,
          crawlDate: date
        },
        {
          name: Utilities.constants.menuItems.LAST_500_FIRST_DEGREE,
          value: graphStats.getMeanDegreeOfNodes(redirectionMappingData, firstPartyData, (d => d.rank > 500)),
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
    $scope.calculate = function (date, instance, alertWhenDone = true, domainData = [], redirectionData = []) {

      const start = moment();
      let requestData = [];
      let domains = [];
      let redirections = [];
      return new Promise(function(resolve) {
        console.log('Calculating stats for instance ' + instance + '...');
        $scope.connection._find(instance,
          {crawlDate: moment(new Date(date)).format(Utilities.constants.DATE_FORMAT)})
          //)
          //.then(data => {
          //  requestData = data;
          //  return $scope.connection._find($scope.connection._redirectionMappingTable);
          //})
          .then(data => {requestData = data;
            return redirectionData.length > 0
              ? Promise.resolve(redirectionData)
              : $scope.connection._find($scope.connection._redirectionMappingTable);})
          .then(data => {redirections = data;
            return domainData.length > 0
              ? Promise.resolve(domainData)
              : $scope.connection._find($scope.connection._firstPartyTable);})
          .then(data => {domains = data; return $scope.connection._find($scope.connection._entityDetailsTable);})
          .then(entityDetails => Promise.resolve(calculationTableService.calculate(requestData, date, instance, entityDetails, domains, redirections)))
          .then((data) => $scope.connection._insertMultiple(data, $scope.connection._statsTable))
          .then((data) => $scope.connection._find($scope.connection._statsTable))
          .then((data) => {
            if (alertWhenDone) {
              alert('Completed stats calculation of instance ' + instance + ' for ' + date + ' in ' + moment().diff(start, 'seconds') + ' seconds. Reload the page to see the results!');
            }

            resolve();
          });
      });
    };
    $scope.calculateAll = function(date) {
      console.log('start');
      let redirections = [];
      let domains = [];
      $scope.connection._find($scope.connection._redirectionMappingTable)
        .then(data => {redirections = data; return $scope.connection._find($scope.connection._firstPartyTable);})
        .then(data => {
          domains = data;
          return Object.values(Utilities.constants.instances).reduce((cum, inst) => {
            console.log('Calculate for instance ' + inst);
            return cum.then(() => $scope.calculate(date, inst, false, domains, redirections));
          }, Promise.resolve());
        })
        .then(() => alert('Finished for date ' + date + '!'));
    };

    $scope.clear = function (date, instance) {
      $scope.connection._find($scope.connection._statsTable, {crawlDate: date, instance: instance})
        .then((data) => data
          // For double security, refilter the data
          .filter(d => d.crawlDate === date)
          .filter(d => d.instance === instance)
          .forEach(d => $scope.connection._delete($scope.connection._statsTable, d._id)));
    };
    $scope.clearAll = function(date) {
      $scope.connection._find($scope.connection._statsTable, {crawlDate: date})
        .then((data) => data
          // For double security, refilter the data
          .filter(d => d.crawlDate === date)
          .forEach(d => $scope.connection._delete($scope.connection._statsTable, d._id)));
    };
  }])
  .directive('calculationTable', function($compile) {
    return {
      template: require('./calculationTable.html')
    };
  })
  .name;
