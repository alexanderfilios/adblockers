import IAttributes = angular.IAttributes;
import IAugmentedJQuery = angular.IAugmentedJQuery;
import IScope = angular.IScope;
import {Region} from '../../core/entities/Region';
import {Rainbow} from './Rainbow';
import 'jvectormap';

/**
 * Created by alexandrosfilios on 24/09/16.
 */

interface IMapParams {
    logColorScale: any;
    width: number;
    height: number;
    numberRangeMin: number;
    numberRangeMax: number;
    spectrum: Array<string>;
}
interface IMapData {
    locations: Array<Location>;
    regions: Array<Region>;
}
class MapData implements IMapData {
    public locations: Array<Location>;
    public regions: Array<Region>;
}
class MapParams implements IMapParams {
    logColorScale: boolean;
    width: number;
    height: number;
    numberRangeMin: number;
    numberRangeMax: number;
    spectrum: Array<string>;
}

interface IMapScope extends IScope, IMapData {}
interface IMapAttributes extends IMapParams {}

export function MapDirective(): ng.IDirective {
    return {
        scope: {
            locations: '=',
            regions: '='
        },
        link: (scope:IMapScope, element:IAugmentedJQuery, attributes:IMapAttributes) => {
            const params = new MapParams();
            params.logColorScale = attributes.logColorScale;
            params.width = attributes.width || 350;
            params.height = attributes.height || 350;
            params.numberRangeMin = attributes.numberRangeMin || 0;
            params.numberRangeMax = attributes.numberRangeMax || 1;
            params.spectrum = attributes.spectrum || ['white', 'yellow', 'red'];

            scope.$watchGroup(['locations', 'regions'], (newValue, oldValue, udpatedScope: IMapScope) => {
                const data = new MapData();
                data.regions = udpatedScope.regions || [];
                data.locations = udpatedScope.locations || [];

                if (data.regions.length > 0 || data.locations.length > 0) {
                    createMap(element, data, params);
                }
            });

        }
    };
};

const createMap: Function = function(element: IAugmentedJQuery,
                                     data: IMapData,
                                     params: IMapParams) {
    
    const rainbow = new Rainbow()
        .setNumberRange(0, 1)
        .setSpectrum('white', 'yellow', 'red');
    
    const markers = data.locations.length === 0
        ? []
        : data.locations.map(location => ({
        latLng: [location.lat, location.lng],
        name: location.country
    }));

    let legend = {};
    if (Array.isArray(data.regions) && data.regions.length > 0) {
        const maxValue = data.regions
            .map(region => region['occurrences'])
            .reduce((max, cur) => Math.max(max, cur), 0);
        legend = {
            legend: {
                vertical: true
            },
            values: [0, 100 * maxValue],
            scale: ['#ffffff', rainbow.colorAt(0), rainbow.colorAt(100 * maxValue)]
        }
    }
    const regions = (data.regions || []).reduce((cum, cur) => {
        cum[cur.country] = '#' + rainbow.colorAt(params.logColorScale
                ? Math.log(100 * cur.occurrences) / Math.log(100)
                : cur.occurrences);
        return cum;
    }, {});


    const mapConfig = {
        map: 'world_mill',
        backgroundColor: '#ffffff',
        scaleColors: ['#C8EEFF', '#0071A4'],
        normalizeFunction: 'log',
        hoverOpacity: 0.7,
        hoverColor: false,
        regionStyle: {
            initial: {
                fill: 'white',
                stroke: '#000000',
                'stroke-width': '0.2',
                'stroke-opacity': '1'
            }
        },
        markerStyle: {
            initial: {
                fill: '#F8E23B',
                stroke: '#383f47',
                r: 4
            }
        },
        series: {
            regions: [{
                values: regions,
                attribute: 'fill'
            },
                legend]
        },
        markers: markers
    };

    (<CSSStyleSheet>document.styleSheets[0])
        .insertRule('.jvectormap-container {height: ' + params.height + 'px !important;}', 1);
    (<CSSStyleSheet>document.styleSheets[0])
        .insertRule('.jvectormap-legend {line-height: 19px !important;}', 1);

    angular.element(element)
        .empty()
        .vectorMap(mapConfig);
};
