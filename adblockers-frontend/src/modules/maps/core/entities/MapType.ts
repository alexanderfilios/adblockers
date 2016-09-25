import {Constants} from "../../../application/core/Constants";
/**
 * Created by alexandrosfilios on 24/09/16.
 */
export class MapType {
    constructor(public name: string) {}
    public static valueOf: Function = function(source:string):MapType {
        return Object.keys(Constants.MAP_TYPES)
                .map(mapTypeName => Constants.MAP_TYPES[mapTypeName])
                .filter(mapType => mapType.name === source)[0]
            || null;
    }
}
