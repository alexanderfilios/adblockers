/**
 * Created by alexandros on 3/17/16.
 */

import jQuery from 'jquery';
import DbCache from './DbCache.js';

const DbConnection = function() {
  console.log('New DB connection object created! Make sure the mongod and mongodb-rest are running.');
  const self = this;
  self._logger = null;
  self._logTable = 'log';
  self._firstPartyTable = 'first_parties';
  self._dataTable = 'data';
  self._database = 'myapp_test1';
  self._host = '127.0.0.1';
  //self._host = '192.33.93.94';
  self._port = 3000;

  // Cache for the default database
  self._cache = new DbCache();
  window.mycache = self._cache;

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
  self._clearCollection = (collection) => self._find(collection)
    .then((data) => data.forEach((record) => self._delete(collection, record._id)));

  self.find = (filter, database = self._database) => self._find(self._dataTable, filter, database);
  self.distinct = (filter, uniqueFields) => self.find(filter).then(data => self._distinct(data, uniqueFields));
  self.getFirstParties = () => self._find(self._firstPartyTable);
  self.getLogs = () => self._find(self._logTable);
  self.clearLogs = () => self._clearCollection(self._logTable);
  self.clearData = () => self._clearCollection(self._dataTable);
  self.store = (data) => self._insert(data, self._dataTable);
  self.log = (message) => self._insert({time: new Date(), message: message}, self._logTable);


  //self._find(self._firstPartyTable, {}, self._database, false, '127.0.0.1').then(data => {
  //  self._clearCollection(self._firstPartyTable);
  //  data.map(o => o.url).forEach(url => {
  //    console.log(url);
  //    self._insert({url: url}, self._firstPartyTable, self._database, '192.33.93.94');
  //  });
  //});
  //self.getFirstParties().then(data => data.map(o => o.url).forEach(url => console.log(url)));
  //self.find().then(data => console.log(data));
};

export default DbConnection;