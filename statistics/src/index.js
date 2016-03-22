import 'angular/angular-csp.css';
import './css/main.css';

import angular from 'angular';

// Load Bootstrap
import jQuery from 'jquery';
import DbConnection from './DbConnection';

window.jQuery = jQuery;

require('bootstrap/dist/js/bootstrap.min');
require('bootstrap/dist/css/bootstrap.min.css');

import demoModule from './demo/demoModule';

import menuModule from './components/menu/menuModule';

angular.module('main', [demoModule, menuModule])
  .controller('MainController', ['$scope', function($scope) {
    $scope.connection = new DbConnection();
    console.log('set a global connection');
  }]);

angular.bootstrap(document.documentElement, ['main']);
