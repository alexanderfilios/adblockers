/**
 * Created by alexandros on 4/27/16.
 */

import angular from 'angular';
require('angular-ui-bootstrap');
const d3 = require('d3');
import {Utilities} from 'adblocker-utils';
import GraphStats from '../../GraphStats';
import jQuery from 'jquery';

export default angular
  .module('mapChart', ['ui.bootstrap'])
  .service('mapService', function() {
    const self = this;
    this.fetchCoordinates = address => new Promise(function (resolve, reject) {
      //console.log('calculating coordinates for address: ' + address);
      //resolve([{geometry: {location: {lat: 1, lng: 2}}}]);
      jQuery.getJSON('http://maps.googleapis.com/maps/api/geocode/json',
        {address: address}, function (data) {
          if (data.status === 'OK') {
            resolve(data.results);
          } else {
            if (data.status === 'ZERO_RESULTS') {
              console.log('Zero results for address: ' + address);
              resolve(data.results);
            } else {
              reject();
            }
          }
        });
    });
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
    this.getMarkers = function (thirdPartyDetails) {
      return thirdPartyDetails
        .slice(0, 20)
        .reduce((cum, cur) => cum.then(data => new Promise(function (resolve) {
          // If location is already calculated, just return it, don't re-calculate it
          if ('location' in cur) {
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
              .then(matches => {
                console.log('NEW: ' + cur.domain + ' -------------------------------');
                const match = !Array.isArray(matches) || matches.length === 0
                  ? {lat: null, lng: null}
                  : matches[0] && matches[0].geometry && matches[0].geometry.location;

                const copiedObject = jQuery.extend({}, cur, {location: match});
                delete copiedObject._id;
                $scope.connection
                  ._update($scope.connection._thirdPartyDetailsTable, cur._id, copiedObject);

                // The _id is not included, but we will get rid of it anyway
                resolve(data.concat(copiedObject));
              });
          }

        })), Promise.resolve([]))
        .then((data) => Promise.resolve(data
            .filter(p => !!p.location.lat && !!p.location.lng)
            .map(p => ({name: p.domain, latLng: [p.location.lat, p.location.lng]}))

        ));
    };
    this.filterThirdPartyMarkers = function(graphStats, markers) {
      if (!graphStats || jQuery.isEmptyObject(graphStats)) {
        return markers;
      }

      // Wrap it in a dict for faster search
      const thirdPartiesDict = Array.from(graphStats.graph.nodes(true))
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
      .then(data => mapService.getMarkers(data))
      .then(markers => {
        console.log(markers);
        $scope.markers = markers;
        $scope.displayedMarkers = mapService.filterThirdPartyMarkers($scope.graphStats, $scope.markers);
        //$scope.$apply();
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
        scope.$watch('graphStats', function() {
          createMap(element, scope.displayedMarkers);
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
        const createMap = function(element, markers) {
          jQuery(element)
            .empty()
            .css('width', '1000px')
            .css('height', '400px')
            .vectorMap({
              map: 'world_mill',
              scaleColors: ['#C8EEFF', '#0071A4'],
              normalizeFunction: 'polynomial',
              hoverOpacity: 0.7,
              hoverColor: false,
              markerStyle: {
                initial: {
                  fill: '#F8E23B',
                  stroke: '#383f47',
                  r: 5
                }
              },
              markers: markers
            });
        }
      }
    }
  }).name;
