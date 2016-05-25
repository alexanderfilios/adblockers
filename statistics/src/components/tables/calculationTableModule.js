/**
 * Created by alexandros on 4/23/16.
 */

import jQuery from 'jquery';
import angular from 'angular';
import {Utilities} from 'adblocker-utils';
import {jStat} from 'jStat';
import moment from 'moment';
import GraphStats from '../../GraphStats';
import JSZip from 'jszip';

export default angular
  .module('calculationTable', ['ui.bootstrap'])
  .service('calculationTableService', function() {

    this.calculate = function(data, date, instance, entityDetails, firstPartyData, redirectionMappingData) {
      const graphStats = new GraphStats(data, entityDetails);

      return {
        //degrees: [],
        degrees: graphStats.getRankDegree(redirectionMappingData, firstPartyData),
          //.map(rd => jQuery.extend({}, rd, {crawlDate, date, instance: instance})),
        stats: [
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
      ]};
    };
  })
  .controller('CalculationTableController', ['$scope', 'calculationTableService', function($scope, calculationTableService) {
    $scope.instances = Utilities.constants.instances;
    $scope.connection._insertMultiple = function (data, collection) {
      return data.reduce((cum, curr) => cum.then(() =>
          $scope.connection._insert(curr, collection)
      ), Promise.resolve());
    };
    $scope.connection._deleteMultiple = function (ids, collection) {
      return ids.reduce((cum, id) => cum.then(() =>
        $scope.connection._delete(collection, id)
      ), Promise.resolve());
    };
    $scope.$watch(scope => scope.calculatedDates, calculatedDates => {
      if (!Utilities.constants.autoCalculate
        || !Array.isArray(calculatedDates)
        || calculatedDates.length === 0) {
        return;
      }
      const dateToCalculate = calculatedDates
        .filter(d => !d.isCalculated || Object.keys(d.isCalculated).length < Object.keys(Utilities.constants.instances).length)
        .map(d => moment(d.date, Utilities.constants.DATE_FORMAT))
        .reduce((min, cur) => moment.min(cur, min), moment())
        .format(Utilities.constants.DATE_FORMAT);

      if (!moment().isSame(moment(dateToCalculate, Utilities.constants.DATE_FORMAT), 'day')) {
        $scope.clearAll(dateToCalculate);
        $scope.calculateAll(dateToCalculate);
      }
    });

    $scope.calculate = function (date, instance, alertWhenDone = true, domainData = [], redirectionData = []) {

      const start = moment();
      let requestData = [];
      let domains = [];
      let redirections = [];

      let statResults = [];
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
          .then((data) => {
            statResults = data.stats;
            const degrees = data.degrees.map(rd => jQuery.extend({}, rd, {crawlDate: date, instance: instance}));
            return $scope.connection._insertMultiple(degrees, $scope.connection._graphDegreeTable)})
          .then((data) => $scope.connection._insertMultiple(statResults, $scope.connection._statsTable))
          .then((data) => $scope.connection._find($scope.connection._statsTable))
          .then((data) => {
            if (alertWhenDone) {
              alert('Completed stats calculation of instance ' + instance + ' for ' + date + ' in ' + moment().diff(start, 'seconds') + ' seconds. Reload the page to see the results!');
            }

            resolve();
          });
      });
    };

    const getScatterplot = function(requestData, redirectionMappingData, firstPartyData) {
      const graphStats = new GraphStats(requestData, false);
      const rankDegree = graphStats.getRankDegree(redirectionMappingData, firstPartyData);

      return {
        fpd: rankDegree.reduce((cum, cur) => {
          cum[cur.rank] = cur.degree;
          return cum;
        }, {}),
        tpd: Object.values(graphStats.getVertexDegrees(false))
      };
    };

    $scope.zipData = null;
    $scope.getAllScatterplots = function(date) {
      $scope.date = date;
      console.log('Start calculating date ' + date);
      const instances = Object.values(Utilities.instances);
      let redirectionMappingData = [];
      let firstPartyData = [];
      const scatterplotData = {};
      $scope.connection._find($scope.connection._redirectionMappingTable)
        .then(data => {
          redirectionMappingData = data;
          return $scope.connection._find($scope.connection._firstPartyTable);})
        .then(data => {
          firstPartyData = data;


          return instances.reduce((cum, instance) => {
              console.log('Calculate instance ' + instance);
              return cum.then(() =>  $scope.connection._find(instance,
                {crawlDate: moment(new Date(date)).format(Utilities.constants.DATE_FORMAT)}))
              .then(requestData => {
                  scatterplotData[instance] = getScatterplot(requestData, redirectionMappingData, firstPartyData);
                  return Promise.resolve();
                })
            }, Promise.resolve());
        })
      .then(() => {
          const fpd = 'Rank,RelativeRank,' + instances.join(',') + '\n'
          + firstPartyData
            .map(f => f.rank)
            .map((r, idx) => r + ',' + (idx + 1) + ',' + instances
              .map(i => i in scatterplotData && r in scatterplotData[i].fpd ? scatterplotData[i].fpd[r] : 0).join(','))
          .join('\n');

          const tpd = instances.map(i =>
            i + ',' + (i in scatterplotData ? scatterplotData[i].tpd.join(',') : '0')).join('\n');

          return new JSZip()
            .file('scatterplot-fpd.csv', fpd)
            .file('scatterplot-tpd.csv', tpd)
            .generateAsync({type: 'blob'});
        })
      .then(zip => {
          $scope.zipData = zip;
          $scope.$apply();
        });

    };

    $scope.calculateAll = function(date) {
      console.log('Start calculating date ' + date);
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
        .then(() => Utilities.constants.autoCalculate
          ? window.location.reload()
          : alert('Finished for date ' + date + '!'));
    };

    $scope.clear = function (date, instance) {
      $scope.connection._find($scope.connection._graphDegreeTable, {crawlDate: date, instance: instance})
        .then((data) => data
          // For double security, refilter the data
          .filter(d => d.crawlDate === date)
          .filter(d => d.instance === instance)
          .forEach(d => $scope.connection._delete($scope.connection._graphDegreeTable, d._id)));
      $scope.connection._find($scope.connection._statsTable, {crawlDate: date, instance: instance})
        .then((data) => data
          // For double security, refilter the data
          .filter(d => d.crawlDate === date)
          .filter(d => d.instance === instance)
          .forEach(d => $scope.connection._delete($scope.connection._statsTable, d._id)));
    };
    $scope.clearAll = function(date) {
      console.log('Clearing data for date ' + date);
      $scope.connection._find($scope.connection._graphDegreeTable, {crawlDate: date})
        .then((data) => $scope.connection._deleteMultiple(data.filter(d => d.crawlDate === date).map(d => d._id), $scope.connection._graphDegreeTable))
        .then(() => $scope.connection._find($scope.connection._statsTable, {crawlDate: date}))
        .then((data) => $scope.connection._deleteMultiple(data.filter(d => d.crawlDate === date).map(d => d._id), $scope.connection._statsTable))
        .then(() => alert('Deleted data for date ' + date));
          //data
          // For double security, refilter the data
          //.filter(d => d.crawlDate === date)
          //.forEach(d => $scope.connection._delete($scope.connection._graphDegreeTable, d._id)));
      //$scope.connection._find($scope.connection._statsTable, {crawlDate: date})
      //  .then((data) => {const ids = data
      //    // For double security, refilter the data
      //    .filter(d => d.crawlDate === date)
      //    .map(d => d._id);
      //  })
      //    .forEach(d => $scope.connection._delete($scope.connection._statsTable, d._id)));
    };
  }])
  .filter('replaceSlashes', function() {
    return (str) => str.replace(/\//g, '-');
  })
  .directive('calculationTable', function($compile) {
    return {
      template: require('./calculationTable.html'),
      link: function(scope, element, attrs) {
        scope.$watch('zipData', function() {
          if (!!scope.zipData) {
            const button = jQuery(element).find('#download-' + moment(scope.date).format(Utilities.constants.DATE_FORMAT).replace(/\//g, '-'))
              .attr('href', window.URL.createObjectURL(scope.zipData))
              .attr('download', 'scatterplots.zip');
            window.button = button;
          }
        });
      }
    };
  })
  .name;
