import 'angular/angular-csp.css';
import './css/main.css';

import angular from 'angular';

// Load Bootstrap
import jQuery from 'jquery';
import {DbConnection} from 'adblocker-utils';

window.jQuery = jQuery;

require('bootstrap/dist/js/bootstrap.min');
require('bootstrap/dist/css/bootstrap.min.css');

import demoModule from './demo/demoModule';

import menuModule from './components/menu/menuModule';

angular.module('main', [demoModule, menuModule])
  .controller('MainController', ['$scope', function($scope) {
    // Setting a global connection
    $scope.connection = new DbConnection();
    //$scope.connection._host = '192.33.93.101';
  }]);

angular.bootstrap(document.documentElement, ['main']);
