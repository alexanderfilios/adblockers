/**
 * Created by alexandrosfilios on 22/09/16.
 */
import IHttpService = angular.IHttpService;
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import {Constants} from "../../../application/core/Constants";

export class MapService {
    public static $inject: Array<string> = ['$http', '$q'];

    httpService: IHttpService;
    qService: IQService;

    constructor(httpService: IHttpService, qService: IQService) {
        this.httpService = httpService;
        this.qService = qService;
    }

    private fetchStats(service: string): IPromise<{[key: string]: number}> {
        const self = this;
        const deferred = self.qService.defer();
        self.httpService({
            method: 'GET',
            url: Constants.BASE_URL + 'scripts/' + service + '/stats'
        })
            .then(result => deferred.resolve(result.data))
            .catch(error => deferred.reject(error));
        return deferred.promise;
    }
    
    public fetchLegalEntityLocationStats(): IPromise<{[key: string]: number}> {
        return this.fetchStats('geocode');
    }
    public fetchServerLocationStats(): IPromise<{[key: string]: number}> {
        return this.fetchStats('geoip');
    }

    private fetchLocations(service: string): IPromise<Array<{[key: string]: string}>> {
        const self = this;
        const deferred = self.qService.defer();
        self.httpService({
            method: 'GET',
            url: Constants.BASE_URL + 'scripts/' + service + '/all/markers'
        })
            .then(result => deferred.resolve(result.data))
            .catch(error => deferred.reject(error));
        return deferred.promise;
    }

    public fetchLegalEntityLocations(): IPromise<Array<{[key: string]: string}>> {
        return this.fetchLocations('geocode');
    }
    public fetchServerLocations(): IPromise<Array<{[key: string]: string}>> {
        return this.fetchLocations('geoip');
    }

    private fetchRegions(service: string): IPromise<Array<{[key: string]: string}>> {
        const self = this;
        const deferred = self.qService.defer();
        self.httpService({
            method: 'GET',
            url: Constants.BASE_URL + 'scripts/' + service + '/all/regions'
        })
            .then(result => deferred.resolve((result.data)))
            .catch(error => deferred.reject(error));
        return deferred.promise;
    }

    public fetchLegalEntityLocationsPerRegion(): IPromise<Array<{[key: string]: string}>> {
        return this.fetchRegions('geocode');
    }
    public fetchServerRegions(): IPromise<Array<{[key: string]: string}>> {
        return this.fetchRegions('geoip');
    }
}
