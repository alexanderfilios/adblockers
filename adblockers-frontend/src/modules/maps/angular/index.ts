/**
 * Created by alexandrosfilios on 22/09/16.
 */
import "angular";
import {MapComponent} from "./components/MapComponent";
import {MapDirective} from "./components/MapDirective";
import "angular-route";
import {config as routesConfig} from "./configs/routes";
import {MapModel} from "../core/models/MapModel";
import {MapService} from "../core/services/MapService";

angular.module("app.maps", ["ngRoute"])
    .directive("map", MapDirective)
    .directive("mapComponent", MapComponent)
    .service("MapModel", MapModel)
    .service("MapService", MapService)
    .config(routesConfig);
