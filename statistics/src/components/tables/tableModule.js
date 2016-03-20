/**
 * Created by alexandros on 3/17/16.
 */

import angular from 'angular';
//const MongoClient = require('mongodb').MongoClient;
//import buttons from 'angular-ui-bootstrap/src/buttons';
import DbConnection from '../../DbConnection';

const conn = new DbConnection();

require('angular-ui-bootstrap');

export default angular
  .module('table', ['ui.bootstrap'])
  .controller('TableController', ['$scope', function($scope) {
    $scope.name = 'My name';
    conn.distinct().then((rows) => $scope.rows = rows.slice(0,1));
  }])
  .directive('myTable', function($compile) {
    return {
      template: require('./table.html'),
      link: function(scope, element, attrs) {
        setTimeout(() => $compile(element.contents())(scope), 1000);
      }
    };
  })
  .name;
