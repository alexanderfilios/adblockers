import {ChartPoint} from "./ChartPoint";
/**
 * Created by alexandrosfilios on 23/09/16.
 */
export class DataSeries {
    constructor(public name: string, public dataPoints: Array<ChartPoint>, public color: string) {}
}
