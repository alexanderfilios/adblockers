/**
 * Created by alexandrosfilios on 24/09/16.
 */
import * as moment from 'moment';
import {Constants} from "../../../application/core/Constants";

export class MetricType {
    constructor(public name: string,
                public title: string,
                public xAxisLabel: string,
                public yAxisLabel: string,
                public description: string,
                public subtitle: string,
                public xTickFormat: Function,
                public yTickFormat: Function
    ) {}

    public static valueOf: Function = function(source:string):MetricType {
        return Object.keys(Constants.METRIC_TYPES)
            .map(metricTypeName => Constants.METRIC_TYPES[metricTypeName])
            .filter(metricType => metricType.name === source)[0]
        || null;
    }
};
