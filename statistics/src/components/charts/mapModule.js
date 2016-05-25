/**
 * Created by alexandros on 4/27/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import {Utilities} from 'adblocker-utils';
import GraphStats from '../../GraphStats';
import jQuery from 'jquery';
import {jStat} from 'jStat';

export default angular
  .module('mapChart', ['ui.bootstrap'])
  .service('mapService', function() {
    const self = this;
    this.getRegions = function(markers) {

      const a = markers.map(m => m.country)
        .reduce((cum, country) => {
          cum[country] = country in cum ? cum[country] + 1 : 1;
          return cum;
        }, {});
      const max = jStat.max(Object.values(a));
      console.log('max: ' + jStat.max(Object.values(a)));
      console.log('sum: ' + jStat.sum(Object.values(a)));
      for (let country in a) {
        const hex = ('0' + Math.floor((1 - a[country]/max) * 255).toString(16)).substr(-2);
        a[country] = '#' + hex + hex + 'ff';
      }
      return a;
    };
    this.fetchCoordinates = address => new Promise(function (resolve, reject) {
      if (!address) {
        resolve([]);
      }
      jQuery.getJSON('http://maps.googleapis.com/maps/api/geocode/json',
        {address: address}, function (data) {
          if (data.status === 'OK') {
            resolve(data.results);
          } else {
            if (data.status === 'ZERO_RESULTS') {
              console.log('Zero results for address: ' + address);
              resolve(data.results);
            } else {
              //console.log('Error: ' + data.status);
              reject(data.status);
            }
          }
        }, function(error) {console.log('error');});
    })
    .then(
        matches => {
          if (!Array.isArray(matches) || matches.length === 0)
            return Promise.resolve({lat: null, lng: null, country: null});

          const match = matches[0] && matches[0].geometry && matches[0].geometry.location;

          return new Promise(function(resolve, reject) {
            jQuery.getJSON('http://maps.googleapis.com/maps/api/geocode/json',
              {latlng: match.lat + ',' + match.lng, sensor: false}, function (data) {
                if (data.status === 'OK') {

                  const countryCode = data.results
                    .reduce((cum, cur) => cum.concat(cur.address_components), [])
                    .filter(ac => ac.types.indexOf('country') > -1)
                    .filter(ac => ac.types.indexOf('political') > -1)
                    .map(ac => ac.short_name)[0];

                  resolve({lat: match.lat, lng: match.lng, country: countryCode});
                } else {
                  if (data.status === 'ZERO_RESULTS') {
                    resolve({lat: match.lat, lng: match.lng, country: null});
                  } else {
                    //console.log('Error: ' + data.status);
                    reject(data.status);
                  }
                }
              });
          });
          //return Promise.resolve(matches)
        },
        errorData => Promise.reject(errorData)
    );
    /**
     * Returns an associative mapping
     * domain (third party) -> {latLng: ..., name: ...}
     * @param thirdPartyDetails Array of objects contianing the third party details
     * [
     *  {domain: ..., location(optional): ..., admin_street(optional): ..., ...},
     *  {domain: ..., location(optional): ..., admin_street(optional): ..., ...}
     * ]
     * @returns {*}
     */
    this.getMarkers = function (thirdPartyDetails, connection) {
      return thirdPartyDetails
        //.slice(0, 600)
        .reduce((cum, cur) => cum.then(data => new Promise(function (resolve) {
          // If location is already calculated, just return it, don't re-calculate it
          if ('location' in cur && 'country' in cur.location) {
            resolve(data.concat(cur));
          } else {
            const address = ['admin_street', 'admin_city', 'admin_country']
                .map(attr => cur[attr])
                .filter(d => d && d.length > 0)
                .join(', ').replace(/_/g, ' ')
              || ['regis_street', 'regis_city', 'regis_country']
                .map(attr => cur[attr])
                .filter(d => d && d.length > 0)
                .join(', ').replace(/_/g, ' ')
              || ['tech_street', 'tech_city', 'tech_country']
                .map(attr => cur[attr])
                .filter(d => d && d.length > 0)
                .join(', ').replace(/_/g, ' ');

            self.fetchCoordinates(address)
              .then(match => {
                const copiedObject = jQuery.extend({}, cur, {location: match});
                delete copiedObject._id;

                connection
                  ._update('third_party_details', cur._id, copiedObject);

                console.log('Fetched coordinates');
                // The _id is not included, but we will get rid of it anyway
                resolve(data.concat(copiedObject));

              },
            errorStatus => {
              console.log('Error occurred: ' + errorStatus);
              resolve(data);
            });
          }

        })), Promise.resolve([]))
        .then((data) => Promise.resolve(data
            .filter(p => !!p.location.lat && !!p.location.lng)
            .map(p => ({country: p.location.country, name: p.domain, latLng: [p.location.lat, p.location.lng]}))

        ));
    };
    this.filterThirdPartyMarkers = function(graphStats, markers) {
      if (!graphStats || jQuery.isEmptyObject(graphStats)) {
        return markers;
      }

      // Wrap it in a dict for faster search
      const thirdPartiesDict = Array.from(graphStats.getGraph().nodes(true))
        .filter(n => !n[1].f)
        .map(n => n[0].slice(2))
        .reduce((cum, n) => {
          cum[n] = true;
          return cum;
        }, {});
      return markers.filter(m => m.name in thirdPartiesDict);
    };
  })
  .controller('MapController', ['$scope', 'mapService', function($scope, mapService) {
    $scope.markers = [];
    $scope.connection._find('third_party_details')
      .then(data => mapService.getMarkers(data, $scope.connection))
      .then(markers => {
        console.log(markers.length + ' found!');
        $scope.markers = markers;
        $scope.regions = mapService.getRegions(markers);
        console.log($scope.regions);
        $scope.displayedMarkers = mapService.filterThirdPartyMarkers($scope.graphStats, $scope.markers);
        $scope.$apply();
      });
    $scope.$watch((scope) => scope.graphStats, (graphStats) => {
      console.log('graph stats loaded');
      $scope.displayedMarkers = mapService.filterThirdPartyMarkers($scope.graphStats, $scope.markers);
      //$scope.$apply();
    });

  }])
  .directive('mapChart', function($compile) {
    return {
      link: function (scope, element, attrs) {
        scope.$watch('markers', function() {
          createMap(element, scope.displayedMarkers, scope.regions);
        });

        /**
         * Creates the map
         * @param element
         * @param markers
         * [
         *  {latLng: [36.778261, -119.4179324], name: 'California'},
         *  {latLng: [41.90, 12.45], name: 'Vatican City'},
         *  {latLng: [43.73, 7.41], name: 'Monaco'}
         * ]
         */
        const createMap = function(element, markers, regions) {
          const vectorMap = jQuery(element)
            .empty()
            .css('width', '1000px')
            .css('height', '400px')
            .vectorMap({
              map: 'world_mill',
              backgroundColor: '#ffffff',
              scaleColors: ['#C8EEFF', '#0071A4'],
              normalizeFunction: 'polynomial',
              hoverOpacity: 0.7,
              hoverColor: false,
              regionStyle: {
                initial: {
                  //fill: '#000000'
                  stroke: '#000000',
                  'stroke-width': '0.2',
                  'stroke-opacity': '1'
                }
              },
              markerStyle: {
                initial: {
                  fill: '#F8E23B',
                  stroke: '#383f47',
                  r: 0
                }
              },
              series: {
                regions: [{
                  //values: {
                  //  US: '#3e9d01'
                  //},
                  values: regions,
                  attribute: 'fill'
                }]
              },
              markers: markers
            });
          window.vm = vectorMap;
        };
      }
    }
  }).name;
