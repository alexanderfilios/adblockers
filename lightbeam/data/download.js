/**
 * Created by alexandros on 3/12/16.
 */
(function (global) {

  const Crawler = function(websites) {
    console.log('New Crawler created!');
    const self = this;
    self._windowOpened = null;
    self._websites = websites;
    self._openWebsite = function(url, delay, callback) {
      return new Promise(function(resolve) {
        if (self._windowOpened !== null) {
          self._windowOpened.close();
        }
        self._windowOpened = window.open(url);
        setTimeout(function() {
          self._windowOpened.close();
          if (typeof callback === 'function') {
            callback(url);
          }
          resolve();
        }, delay);
      });
    };
    self.crawl = function(callback, delay = 2000) {
      self._websites.reduce(function(currentPromise, nextUrl) {
        return currentPromise
            .then(() => self._openWebsite(nextUrl, delay, callback));
      }, Promise.resolve())
        .then(() => self._windowOpened.close());
    }
  };

  const Synchronizer = function(dbConnection, interval = 3000) {
    console.log('New synchronizer created!');
    const self = this;
    self._dbConnection = dbConnection;
    self._connectionsSent = 0;
    self._arrayToJson = function(data) {
      // Translates data in array mode to JSON object according to the constants defined in lightbeam.js, so that they are readable and ready to be stored in mongo db.
      return {
        source: data[0],
        target: data[1],
        timestamp: data[2],
        contentType: data[3],
        cookie: data[4],
        sourceVisited: data[5],
        secure: data[6],
        sourcePathDepth: data[7],
        sourceQueryDepth: data[8],
        sourceSub: data[9],
        targetSub: data[10],
        method: data[11],
        status: data[12],
        cacheable: data[13],
        fromPrivateMode: data[14]
      };
    };
    self.storeNewConnections = function(url) {
      if (self._connectionsSent < global.allConnections.length) {
        console.log('Found ' + (global.allConnections.length - self._connectionsSent) + ' connections for first party: ' + url);
        global.allConnections
          // Get only new connections (the last connections stored in global.allConnections)
          .slice(self._connectionsSent - global.allConnections.length)
          // Convert them to JSON objects
          .map((data) => self._arrayToJson(data))
          // Append the first party URL (the one given by lightbeam is based on heuristics)
          .map((data) => jQuery.extend(data, {firstParty: url}))
          // Store them into DB
          .forEach((data) => self._dbConnection.store(data));

        self._connectionsSent = global.allConnections.length;
      } else {
        console.log('There are no new connections');
      }
    };
  };

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
    self._clearCollection = (collection) => self._find(collection)
      .then((data) => data.forEach((record) => self._delete(collection, record._id)));

    self.find = (filter) => self._find(self._dataTable, filter);
    self.getFirstParties = () => self._find(self._firstPartyTable);
    self.getLogs = () => self._find(self._logTable);
    self.clearLogs = () => self._clearCollection(self._logTable);
    self.clearData = () => self._clearCollection(self._dataTable);
    self.store = (data) => self._insert(data, self._dataTable);
    self.log = (message) => self._insert({time: new Date(), message: message}, self._logTable);
  };

  const db = new DbConnection();
  db.clearLogs();
  db.clearData();
  const sync = new Synchronizer(db);
  db.getFirstParties().then(function(data) {
    new Crawler(data.map((record) => record.url))
      .crawl(sync.storeNewConnections, 5000);
  });

  db.getFirstParties().then((data) => console.log(data));

  //db._find('first_parties', null, 'tracker').then((data) => data.forEach((record) => db._insert({url: record.url}, 'first_parties')));

})(this);