/**
 * Created by alexandrosfilios on 23/09/16.
 */
import {GraphType} from "../../angular/components/LineChartComponent";
import {ChartPoint} from "../entities/ChartPoint";
import {LineChartService} from "../services/LineChartService";
import ILogService = angular.ILogService;
import {GraphData} from "../entities/GraphData";
import {DataSeries} from "../entities/DataSeries";
import {MetricType} from "../entities/MetricType";
import {Rainbow} from "../../../maps/angular/components/Rainbow";

export class LineChartModel {
    public static $inject:Array<string> = ['LineChartService', '$log'];
    private lineChartService:LineChartService;
    private logger:ILogService;
    public graphs:Array<GraphData>;

    public chartPoints:{[key:string]:{[key:string]:number}};

    constructor(lineChartService:LineChartService, logger:ILogService) {
        this.lineChartService = lineChartService;
        this.logger = logger;
    }

    public fetchMetric(graphType:GraphType, metricType:MetricType):void {
        const self = this;
        self.graphs = null;
        self.chartPoints = null;
        self.lineChartService.fetchMetrics(graphType && graphType.name || null, metricType && metricType.name || null)
            .then(data => self.graphs = Object.keys(data)
                .map((metricTypeName, idx:number) => {
                    const dataSeriesSize:number = Object.keys(data[metricTypeName]).length;
                    const rainbow = new Rainbow()
                        .setNumberRange(0, dataSeriesSize - 1)
                        .setSpectrum('white', 'yellow', 'red', 'blue', 'green');
                    const dataSeriesCollection:Array<DataSeries> = Object.keys(data[metricTypeName])
                        .map((dataSeriesName, idx) => {
                            const dataSeries:Array<ChartPoint> = Object.keys(data[metricTypeName][dataSeriesName])
                                .map(date => new ChartPoint(parseFloat(date), data[metricTypeName][dataSeriesName][date]))
                                .sort((p1, p2) => p1.x - p2.x);
                            return new DataSeries(dataSeriesName, dataSeries, rainbow.colourAt(idx));
                        });
                    const metricType = MetricType.valueOf(metricTypeName);
                    const graphData = new GraphData(metricType && metricType.title || metricTypeName, dataSeriesCollection);
                    graphData.caption = '<strong>Figure ' + (idx + 1) + '.</strong> '
                        + (metricType && metricType.description || '');
                    graphData.xAxisLabel = metricType && metricType.xAxisLabel || '';
                    graphData.yAxisLabel = metricType && metricType.yAxisLabel || '';
                    graphData.subtitle = metricType && metricType.subtitle || '';
                    graphData.xTickFormat = metricType && metricType.xTickFormat || ((x) => x);
                    graphData.yTickFormat = metricType && metricType.yTickFormat || ((y) => y);

                    return graphData;
                }))
            .catch(error => self.logger.error(error));
    }
}
