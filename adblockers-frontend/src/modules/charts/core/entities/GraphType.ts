import {Constants} from "../../../application/core/Constants";
/**
 * Created by alexandrosfilios on 24/09/16.
 */
export class GraphType {
    constructor(public name: string) {}
    public static valueOf: Function = function(source:string):GraphType {
        return Object.keys(Constants.GRAPH_TYPES)
                .map(graphTypeName => Constants.GRAPH_TYPES[graphTypeName])
                .filter(graphType => graphType.name === source)[0]
            || null;
    }
}
