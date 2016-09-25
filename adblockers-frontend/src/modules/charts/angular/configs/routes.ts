/**
 * Created by alexandrosfilios on 23/09/16.
 */
import {LineChartDirective, LineChartController} from '../components/LineChartComponent';

config.$inject = ["$routeProvider"];
export function config($routeProvider: ng.route.IRouteProvider): void {
    $routeProvider.when("/linecharts/:graphTypeName/metric/:metricTypeName", {
        template:
            `<h2>Line Charts</h2>
            <div>Some short description</div>
            <line-chart></line-chart>`,
        controller: LineChartController
    }).when("/linecharts/:graphTypeName", {
        template:
            `<h2>Line Charts</h2>
            <div>Some short description</div>
            <line-chart></line-chart>`,
        controller: LineChartController
    }).otherwise({
        redircetTo: "/"
    });
}
