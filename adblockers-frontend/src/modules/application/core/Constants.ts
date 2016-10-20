import {MapType} from "../../maps/core/entities/MapType";
import {GraphType} from "../../charts/core/entities/GraphType";
import {MetricType} from "../../charts/core/entities/MetricType";
import * as moment from 'moment';
/**
 * Created by alexandrosfilios on 25/09/16.
 */

export class Constants {
    public static BASE_URL: string = 'http://localhost:8080/';

    public static MAP_TYPES:{[key:string]:MapType} = {
        LEGAL_ENTITIES_MARKERS: new MapType('0'),
        LEGAL_ENTITIES_REGIONS: new MapType('1'),
        SERVERS_MARKERS: new MapType('2'),
        SERVERS_REGIONS: new MapType('3')
    };
    public static GRAPH_TYPES:{[key:string]:GraphType} = {
        ENTITY_GRAPH: new GraphType('entitygraph'),
        DOMAIN_GRAPH: new GraphType('domaingraph')
    };
    public static METRIC_TYPES:{[key:string]:MetricType} = {
        FPD_DEGREE: new MetricType(
            'FPD_DEGREE_MEAN',
            'FPD node degree',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        FPD_DEGREE_TOP_500: new MetricType(
            'FPD_DEGREE_MEAN_TOP_500',
            'FPD node degree (top 500)',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        FPD_DEGREE_LAST_500: new MetricType(
            'FPD_DEGREE_MEAN_LAST_500',
            'FPD node degree (last 500)',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        FPD_DEGREE_STDEV: new MetricType(
            'FPD_DEGREE_STDEV',
            'FPD node degree (stdev)',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        FPD_DEGREE_TOP_10: new MetricType(
            'FPD_DEGREE_MEAN_TOP_10',
            'FPD node degree (top 10)',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        FPD_DEGREE_TOP_1: new MetricType(
            'FPD_DEGREE_MEAN_TOP_1',
            'FPD node degree (top 1)',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        FPD_DEGREE: new MetricType(
            'FPD_DEGREE_MEAN',
            'FPD node degree',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        TPD_DEGREE: new MetricType(
            'TPD_DEGREE_MEAN',
            'TPD node degree',
            'Date',
            'TPD node degree',
            'The TPD degree',
            'Subtitle for the TPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        TPD_DEGREE_STDEV: new MetricType(
            'TPD_DEGREE_STDEV',
            'TPD node degree (stdev)',
            'Date',
            'TPD node degree',
            'The TPD degree',
            'Subtitle for the TPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        TPD_DEGREE_TOP_10: new MetricType(
            'TPD_DEGREE_MEAN_TOP_10',
            'TPD node degree (top 10)',
            'Date',
            'TPD node degree',
            'The TPD degree',
            'Subtitle for the TPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        TPD_DEGREE_TOP_1: new MetricType(
            'TPD_DEGREE_MEAN_TOP_1',
            'TPD node degree (top 1)',
            'Date',
            'TPD node degree',
            'The TPD degree',
            'Subtitle for the TPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        DENSITY: new MetricType(
            'DENSITY',
            'Graph density',
            'Date',
            'Density',
            'The density',
            'Subtitle for the graph density',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(5)
        )
    }

}
