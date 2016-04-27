import 'angular/angular-csp.css';
import './css/main.css';

import angular from 'angular';

// Load Bootstrap
import jQuery from 'jquery';
import {DbConnection} from 'adblocker-utils';

window.jQuery = jQuery;

require('bootstrap/dist/js/bootstrap.min');
require('bootstrap/dist/css/bootstrap.min.css');
//import a from './jvm/jquery-jvectormap-2.0.3.css'
//import a from './jvm/jquery-jvectormap-2.0.3.min.js'
//import a from './jvm/jquery-jvectormap-world-mill.js'
require('./jvm/jquery-jvectormap-2.0.3.css');

require('./jvm/jquery-jvectormap-2.0.3.min.js');
require('./jvm/jquery-jvectormap-world-mill.js');


import demoModule from './demo/demoModule';

import menuModule from './components/menu/menuModule';

angular.module('main', [demoModule, menuModule])
  .controller('MainController', ['$scope', function($scope) {
    // Setting a global connection
    $scope.connection = new DbConnection();
    //$scope.connection._host = '192.33.93.101';

    //TODO
    $scope.connection._thirdPartyDetailsTable = 'third_party_details';
    $scope.connection._update = function(collection, id, data, database = $scope.connection._database, host = $scope.connection._host, port = $scope.connection._port) {
      return new Promise(function (resolve, reject) {
        jQuery.ajax({
          type: 'PUT',
          url: 'http://' + host + ':' + port + '/' + database + '/' + collection + '/' + id,
          dataType: 'json',
          data: data,
          success: (result) => resolve(result),
          error: (xhr, status, error) => reject(xhr, status, error)
        });
      });

    }
  }]);

angular.bootstrap(document.documentElement, ['main']);
