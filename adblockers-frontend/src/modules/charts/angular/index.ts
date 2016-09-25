/**
 * Created by alexandrosfilios on 23/09/16.
 */
import "angular";
import {LineChartDirective} from './components/LineChartComponent';
import "angular-route";
import "angular-nvd3";
import {config as routesConfig} from "./configs/routes";
import {LineChartModel} from '../core/models/LineChartModel';
import {LineChartService} from '../core/services/LineChartService';

angular.module("app.charts", ['ngRoute', 'nvd3'])
    .directive('lineChart', LineChartDirective)
    .service('LineChartModel', LineChartModel)
    .service('LineChartService', LineChartService)
    .config(routesConfig);
