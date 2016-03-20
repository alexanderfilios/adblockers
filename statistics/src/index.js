import 'angular/angular-csp.css';
import './css/main.css';

import angular from 'angular';

// Load Bootstrap
import jQuery from 'jquery';
window.jQuery = jQuery;
require('bootstrap/dist/js/bootstrap.min');
require('bootstrap/dist/css/bootstrap.min.css');

import demoModule from './demo/demoModule';

import menuModule from './components/menu/menuModule';

angular.module('main', [
  demoModule, menuModule
]);

angular.bootstrap(document.documentElement, ['main']);
