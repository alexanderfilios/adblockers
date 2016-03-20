/**
 * Created by alexandros on 3/17/16.
 */

  import jQuery from 'jquery';

const DbConnection = function() {
  console.log('New DB connection object created! Make sure the mongod and mongodb-rest are running.');
  const self = this;
  self._logger = null;
  self._logTable = 'log';
  self._firstPartyTable = 'first_parties';
  self._dataTable = 'data';
  self._database = 'myapp_test1';
  self._host = '127.0.0.1';
  self._port = 3000;

  self._find = function(collection, filter, database) {
    let url = 'http://' + self._host + ':' + self._port + '/' + (database || self._database) + '/' + collection;
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
        success: (result) => resolve(result),
        error: (xhr, status, error) => reject(xhr, status, error)
      });
    });
  };
  self._delete = function(collection, id, database) {
    return new Promise(function(resolve, reject) {
      jQuery.ajax({
        type: 'DELETE',
        url: 'http://' + self._host + ':' + self._port + '/' + (database || self._database) + '/' + collection + (id ? ('/' + id) : ''),
        dataType: 'json',
        success: (result) => resolve(result),
        error: (xhr, status, error) => reject(xhr, status, error)
      });
    });
  };
  self._insert = function(data, collection, database) {
    return new Promise(function(resolve, reject) {
      jQuery.ajax({
        type: 'POST',
        url: 'http://' + self._host + ':' + self._port + '/' + (database || self._database) + '/' + collection,
        contentType: 'application/json',
        data: JSON.stringify(data),
        dataType: 'json',
        success: (result) => resolve(result),
        error: (xhr, status, error) => reject(xhr, status, error)
      });
    });
  };
  self._distinct = (array = [], uniqueFields) => array
    .filter((currItem, idx) => !array.slice(0, idx)
      .some(prevItem => self._equal(prevItem, currItem, uniqueFields)));
  self._equal = (obj1, obj2, fields = []) => fields.length === 0
    ? (obj1 === obj2)
    : (fields.every(field => obj1[field] === obj2[field]));
  self._clearCollection = (collection) => self._find(collection)
    .then((data) => data.forEach((record) => self._delete(collection, record._id)));

  self.find = (filter) => self._find(self._dataTable, filter);
  self.distinct = (filter, uniqueFields) => self.find(filter).then(data => self._distinct(data, uniqueFields));
  self.getFirstParties = () => self._find(self._firstPartyTable);
  self.getLogs = () => self._find(self._logTable);
  self.clearLogs = () => self._clearCollection(self._logTable);
  self.clearData = () => self._clearCollection(self._dataTable);
  self.store = (data) => self._insert(data, self._dataTable);
  self.log = (message) => self._insert({time: new Date(), message: message}, self._logTable);
};

export default DbConnection;
