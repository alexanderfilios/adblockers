/**
 * Created by alexandrosfilios on 23/09/16.
 */
import IScope = angular.IScope;
import IRoute = angular.route.IRoute;
import ICompileService = angular.ICompileService;
import ITemplateRequestService = angular.ITemplateRequestService;
import IAugmentedJQuery = angular.IAugmentedJQuery;
import IAttributes = angular.IAttributes;
import IRouteParamsService = angular.route.IRouteParamsService;
import {LineChartModel} from "../../core/models/LineChartModel";
import {GraphData} from "../../core/entities/GraphData";
import {MetricType} from "../../core/entities/MetricType";
import {GraphType} from "../../core/entities/GraphType";

export function LineChartDirective(): ng.IDirective {
    return {
        template: `
            <div>
                <nvd3 ng-repeat="graphParams in graphParamsCollection track by $index"
                    options="graphParamsCollection[$index].options"
                    data="graphParamsCollection[$index].data"></nvd3>
            </div>`
    };
};

interface ILineChartScope extends IScope {
    graphParamsCollection:any;
    lineChartModel:LineChartModel;
}
interface ILineChartRouteParamsService extends IRouteParamsService {
    metricTypeName: string;
    graphTypeName: string;
}
export class LineChartController {
    public static $inject: Array<string> = ['LineChartModel', '$scope', '$routeParams'];

    constructor(public lineChartModel: LineChartModel,
                private scope: ILineChartScope,
                private routeParamsService: ILineChartRouteParamsService) {

        const self = this;
        scope.lineChartModel = lineChartModel;
        scope.graphParamsCollection = [];
        lineChartModel.fetchMetric(
            GraphType.valueOf(routeParamsService.graphTypeName),
            MetricType.valueOf(routeParamsService.metricTypeName) || null);

        scope.$watch('lineChartModel.graphs', () => {
            if (Array.isArray(lineChartModel.graphs) && lineChartModel.graphs.length > 0) {
                scope.graphParamsCollection = self.lineChartModel.graphs.map(graph => ({
                    data: getDataForGraph(graph),
                    options: getOptionsForGraph(graph)
                }));
            }
        });
    }
};

const getDataForGraph: Function = function(graphData: GraphData): Array<{[key: string]: any}> {
    return graphData.dataSeriesCollection
        .map(dataSeries => ({
            key: dataSeries.name,
            values: dataSeries.dataPoints.map(dataPoint => ({x: dataPoint.x, y: dataPoint.y})),
            color: dataSeries.color
        }));
};

const getOptionsForGraph:Function = function (graph:GraphData):any {
    return {
        chart: {
            type: 'lineChart',
            height: 450,
            margin: {
                top: 20,
                right: 20,
                bottom: 40,
                left: 55
            },
            x: function (d) {
                return d.x;
            },
            y: function (d) {
                return d.y;
            },
            useInteractiveGuideline: true,
            dispatch: {
                stateChange: function (e) {
                    console.log("stateChange");
                },
                changeState: function (e) {
                    console.log("changeState");
                },
                tooltipShow: function (e) {
                    console.log("tooltipShow");
                },
                tooltipHide: function (e) {
                    console.log("tooltipHide");
                }
            },
            xAxis: {
                axisLabel: graph.xAxisLabel,
                tickFormat: graph.xTickFormat
            },
            yAxis: {
                axisLabel: graph.yAxisLabel,
                tickFormat: graph.yTickFormat,
                axisLabelDistance: -10
            },
            callback: function (chart) {
                console.log("!!! lineChart callback !!!");
            }
        },
        title: {
            enable: true,
            text: graph.title
        },
        subtitle: {
            enable: true,
            text: graph.subtitle,
            css: {
                'text-align': 'center',
                'margin': '10px 13px 0px 7px'
            }
        },
        caption: {
            enable: true,
            html: graph.caption,
            css: {
                'text-align': 'justify',
                'margin': '10px 13px 0px 7px'
            }
        }
    }
};
