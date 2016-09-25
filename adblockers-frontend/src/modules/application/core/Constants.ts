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
            'FPD_DEGREE',
            'FPD node degree',
            'Date',
            'FPD node degree',
            'The FPD degree',
            'Subtitle for the FPD degree',
            x => moment(x, 'DD-MM-YYYY').format('DD-MM'),
            y => y.toFixed(2)
        ),
        TPD_DEGREE: new MetricType(
            'TPD_DEGREE',
            'TPD node degree',
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
            y => y.toFixed(2)
        )
    }
    
}
