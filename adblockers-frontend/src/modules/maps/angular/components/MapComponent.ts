/**
 * Created by alexandrosfilios on 22/09/16.
 */
import {MapModel} from "../../core/models/MapModel";
import IScope = angular.IScope;
import "jvectormap";
import "../../resources/jquery-jvectormap-world-mill.js";
import "../../../../styles/jquery-jvectormap.css";
import IRoute = angular.route.IRoute;
import ICompileService = angular.ICompileService;
import ITemplateRequestService = angular.ITemplateRequestService;
import IAugmentedJQuery = angular.IAugmentedJQuery;
import IAttributes = angular.IAttributes;
import IRouteParamsService = angular.route.IRouteParamsService;
import {Constants} from "../../../application/core/Constants";

export function MapComponent(): ng.IDirective {
    return {
        template: `
            <div>
                <map regions="mapModel.regions" locations="mapModel.locations" log-color-scale="true"></map>
            </div>`,
    };
}

interface IMapScope extends IScope {
    mapModel: MapModel;
}
interface IMapRouteParamsService {
    mapTypeName: string;
}
export class MapController {
    public static $inject:Array<string> = ["MapModel", "$scope", "$routeParams"];

    constructor(public mapModel:MapModel,
                private scope:IMapScope,
                private routeParamsService:IMapRouteParamsService) {
        scope.mapModel = mapModel;

        switch (routeParamsService.mapTypeName) {
            case Constants.MAP_TYPES['LEGAL_ENTITIES_MARKERS'].name:
                mapModel.fetchLegalEntityLocations();
                mapModel.fetchLegalEntityLocationStats();
                break;
            case Constants.MAP_TYPES['SERVERS_MARKERS'].name:
                mapModel.fetchServerLocations();
                mapModel.fetchServerLocationStats();
                break;
            case Constants.MAP_TYPES['LEGAL_ENTITIES_REGIONS'].name:
                mapModel.fetchLegalEntityRegions();
                mapModel.fetchLegalEntityLocationStats();
                break;
            case Constants.MAP_TYPES['SERVERS_REGIONS'].name:
                mapModel.fetchServerRegions();
                mapModel.fetchServerLocationStats();
                break;
        }
    }
};


