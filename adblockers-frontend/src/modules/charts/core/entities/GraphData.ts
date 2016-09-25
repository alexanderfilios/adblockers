import {DataSeries} from "./DataSeries";
/**
 * Created by alexandrosfilios on 23/09/16.
 */
export class GraphData {
    public xAxisLabel: string = '';
    public yAxisLabel: string = '';
    public caption: string = '';
    public subtitle: string = '';
    public xTickFormat: Function = x => x;
    public yTickFormat: Function = y => y;
    constructor(public title: string, public dataSeriesCollection: Array<DataSeries>) {}
}
