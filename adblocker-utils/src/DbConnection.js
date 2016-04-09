/**
 * Created by alexandros on 3/17/16.
 */

import jQuery from 'jquery';
import DbCache from './DbCache.js';
import Utilities from './Utilities.js';
import ProfileConfigs from './ProfileConfigs.js';
import moment from 'moment';

const DbConnection = function(profile = 'default') {
  console.log('New DB connection object created! Make sure the mongod and mongodb-rest are running.');
  const self = this;
  self._config = ProfileConfigs[profile] || ProfileConfigs.default;
  self._logger = null;
  self._logTable = 'log';
  self._firstPartyTable = 'first_parties';
  self._dataTable = self._config.DATA_COLLECTION;
  self._statsTable = 'statistics';
  self._database = self._config.DATABASE_NAME;
  self._host = self._config.DATABASE_HOST;
  self._port = self._config.DATABASE_PORT;

  // Cache for the default database
  self._cache = new DbCache();

  self._find = function(collection, filter = {}, database = self._database, cache = true, host = self._host, port = self._port) {
    let cachedData = null;
    // We asked for caching and data is cached
    if (cache && database === self._database
      && (cachedData = self._cache.get(collection, filter)) !== null) {
      console.log('Returning cached data for filter ' + JSON.stringify(filter));
      return new Promise((resolve) => resolve(cachedData.data));
    }

    let url = 'http://' + host + ':' + port + '/' + database + '/' + collection;
    if (typeof filter === 'string') {
      // Filter is an ID
      url += '/' + filter;
    } else if (!jQuery.isEmptyObject(filter)) {
      // Filter is an object
      url += '?query=' + JSON.stringify(filter);
    }
    return new Promise(function (resolve, reject) {
      jQuery.ajax({
        type: 'GET',
        url: url,
        dataType: 'json',
        success: function(result) {
          console.log('Fetching uncached data for filter ' + JSON.stringify(filter));
          if (cache) {
            self._cache.put(collection, filter, result);
          }
          resolve(result);
        },
        error: (xhr, status, error) => reject(xhr, status, error)
      });
    });
  };
  self._delete = function(collection, id, database = self._database, host = self._host, port = self._port) {
    return new Promise(function(resolve, reject) {
      jQuery.ajax({
        type: 'DELETE',
        url: 'http://' + host + ':' + port + '/' + database + '/' + collection + (id ? ('/' + id) : ''),
        dataType: 'json',
        success: (result) => resolve(result),
        error: (xhr, status, error) => reject(xhr, status, error)
      });
    });
  };
  self._insert = function(data, collection, database = self._database, host = self._host, port = self._port) {
    return new Promise(function(resolve, reject) {
      jQuery.ajax({
        type: 'POST',
        url: 'http://' + host + ':' + port + '/' + database + '/' + collection,
        contentType: 'application/json',
        data: JSON.stringify(data),
        dataType: 'json',
        success: (result) => resolve(result),
        error: (xhr, status, error) => reject(xhr, status, error)
      });
    });
  };
  self._distinct = (array = [], uniqueFields = []) => array
    .filter((currItem, idx) => !array.slice(0, idx)
      .some(prevItem => self._equal(prevItem, currItem, uniqueFields)));
  self._equal = (obj1, obj2, fields = []) => fields.length === 0
    ? (obj1 === obj2)
    : (fields.every(field => obj1[field] === obj2[field]));
  self._clearCollection = (collection, filter = {}) => self._find(collection, filter)
    .then((data) => data.forEach((record) => self._delete(collection, record._id)));

  self.findOrCalculateStats = function(stat,
                                       calculator = (data) => 0,
                                       startDate = moment().format(Utilities.constants.DATE_FORMAT),
                                       endDate = moment().format(Utilities.constants.DATE_FORMAT),
                                       filter = {}) {

    const daysBetween = moment.duration(moment(endDate, Utilities.constants.DATE_FORMAT)
      .diff(moment(startDate, Utilities.constants.DATE_FORMAT))).get('days');
    return new Promise(function (resolve) {
      self._find(self._statsTable, jQuery.extend({}, {name: stat}, filter))
        .then(function (data) {

          const datesNotCalculated = Array.apply(null, Array(daysBetween))
            .map((el, idx) => idx)
            .map(el => moment(endDate, Utilities.constants.DATE_FORMAT).subtract(el, 'days').format(Utilities.constants.DATE_FORMAT))
            .filter(date => !data.some(d => d.date === date && d.name === stat));

          const newData = [];
          Utilities.executeSerially(
            datesNotCalculated,
            (input, output) => newData.push({date: input, name: stat, value: calculator(output)}),
            (date) => self.find(jQuery.extend({}, {crawlDate: date}, filter))
          )
            .then(() => Utilities.executeSerially(
              newData,
              (input, output) => console.log('Storing data for ' + input.name + ' on ' + input.date),
              (input) => self._insert(input, self._statsTable)
            ))
            .then(() => resolve(data.concat(newData)));
        });
    });

  };
  self.find = (filter, database = self._database) => self._find(self._dataTable, filter, database);
  self.distinct = (filter, uniqueFields) => self.find(filter).then(data => self._distinct(data, uniqueFields));
  self.getFirstParties = () => self._find(self._firstPartyTable);
  self.getLogs = () => self._find(self._logTable);
  self.clearLogs = (filter = {}) => self._clearCollection(self._logTable, filter);
  self.clearData = (filter = {}) => self._clearCollection(self._dataTable, filter);
  self.clearStats = (filter = {}) => self._clearCollection(self._statsTable, filter);
  self.store = (data) => self._insert(data, self._dataTable);
  self.log = (message) => self._insert({time: new Date(), message: message}, self._logTable);


};

export default DbConnection;
