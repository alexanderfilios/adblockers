/**
 * Created by alexandrosfilios on 22/09/16.
 */
import {MapController} from "../components/MapComponent";

config.$inject = ["$routeProvider"];
export function config($routeProvider: ng.route.IRouteProvider): void {
    $routeProvider.when("/maps/:mapTypeName", {
        template:
            `<h2>Map</h2>
            <div>Some short description</div>
            <map-component></map-component>`,
        controller: MapController
    }).otherwise({
        redircetTo: "/"
    });
}
