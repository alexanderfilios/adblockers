/**
 * Created by alexandrosfilios on 23/09/16.
 */
import IHttpService = angular.IHttpService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import {Constants} from "../../../application/core/Constants";

export class LineChartService {
    public static $inject:Array<string> = ['$http', '$q'];

    private httpService:IHttpService;
    private qService:IQService;

    constructor(httpService:IHttpService, qService:IQService) {
        this.httpService = httpService;
        this.qService = qService;
    }

    public fetchMetrics(graphTypeName: string, metricTypeName: string): IPromise<Array<{[key: string]: string}>> {
        const self = this;
        const deferred = self.qService.defer();
        self.httpService({
            method: 'GET',
            url: Constants.BASE_URL + 'metrics/' + graphTypeName
            + (metricTypeName !== null ? '/' + metricTypeName : '')
        })
            .then(result => deferred.resolve(result.data))
            .catch(error => deferred.reject(error));
        return deferred.promise;
    }
}
