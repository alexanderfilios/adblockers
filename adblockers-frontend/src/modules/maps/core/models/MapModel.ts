/**
 * Created by alexandrosfilios on 22/09/16.
 */
import {Location} from "../entities/Location";
import {MapService} from '../services/MapService';
import ILogService = angular.ILogService;
import IScope = angular.IScope;
import IHttpPromise = angular.IHttpPromise;
import IPromise = angular.IPromise;
import IQService = angular.IQService;
import {Region} from "../entities/Region";

export class MapModel {
    public static $inject: Array<string> = ["MapService", "$q", "$log"];
    private locations: Array<Location>;
    private regions: Array<Region>;
    private stats: {[key: string]: number};
    private mapService: MapService;
    private logger: ILogService;
    private qService: IQService;

    constructor(mapService: MapService, qService: IQService, logger: ILogService) {
        this.mapService = mapService;
        this.logger = logger;
        this.qService = qService;
    }

    public fetchLegalEntityLocationStats(): void {
        const self = this;
        self.stats = null;
        self.mapService.fetchLegalEntityLocationStats()
            .then(stats => self.stats = stats)
            .catch(error => self.logger.error(error));
    }
    
    public fetchServerLocationStats(): void {
        const self = this;
        self.stats = null;
        self.mapService.fetchServerLocationStats()
            .then(stats => self.stats = stats)
            .catch(error => self.logger.error(error));
    }
    
    public fetchLegalEntityLocations(): void {
        const self = this;
        self.locations = null;
        self.regions = null;
        self.mapService.fetchLegalEntityLocations()
            .then(locations => self.locations = locations.map(location => new Location(
                location['country'],
                parseFloat(location['latitude']),
                parseFloat(location['longitude']))))
            .catch(error => self.logger.error(error));
    }
    public fetchServerLocations(): void {
        const self = this;
        self.locations = null;
        self.regions = null;
        self.mapService.fetchServerLocations()
            .then(locations => self.locations = locations.map(location => new Location(
                location['country'],
                parseFloat(location['latitude']),
                parseFloat(location['longitude']))))
            .catch(error => self.logger.error(error));
    }
    public fetchLegalEntityRegions(): void {
        const self = this;
        self.locations = null;
        self.regions = null;
        self.mapService.fetchLegalEntityLocationsPerRegion()
            .then(regions => self.regions = regions.map(region => new Region(
                region['country'],
                parseFloat(region['occurrences']))))
            .catch(error => self.logger.error(error));
    }
    public fetchServerRegions(): void {
        const self = this;
        self.locations = null;
        self.regions = null;
        self.mapService.fetchServerRegions()
            .then(regions => self.regions = regions.map(region => new Region(
                region['country'], parseFloat(region['occurrences']))))
            .catch(error => self.logger.error(error));
    }
    
}

