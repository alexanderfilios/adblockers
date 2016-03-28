/**
 * Created by alexandros on 3/12/16.
 */
(function (global) {

  const setTimeoutPromise = function(callback, timeout) {
    return new Promise(function(resolve) {
      setTimeout(function() {
        if (typeof callback === 'function') {
          callback();
        }
        resolve();
      }, timeout);
    });
  };

  //setTimeoutPromise(() => console.log('first'), 1000)
  //.then(() => setTimeoutPromise(() => console.log('second'), 3000))
  //.then(() => setTimeoutPromise(() => console.log('third'), 3000));

  const delay = function(interval) {
    return new Promise(function(resolve) {
      setTimeout(() => resolve(), interval);
    });
  };

  const Logger = function() {
    const self = this;
    self._conn = new DbConnection();
    self._debugActive = true;
    self.debug = function(...args) {
      if (self._debugActive) {
        console.log(...args);
      }
    };
    self.log = self._conn.log;
  };

  const Crawler = function(websites) {
    const self = this;
    self._logger = new Logger();
    self._logger.debug('New Crawler created!');

    self._windowOpened = null;
    self._websites = websites;
    self._closeWindow = function() {
      if (self._windowOpened !== null) {
        self._logger.debug('Closing window');
        self._windowOpened.close();
        self._windowOpened = null;
      }
    };
    self._openWebsite = function(url, callback, windowOpenInterval, storeDataInterval) {
      return new Promise(function(resolve) {
        self._logger.debug('Open window ' + url);
        // Close window if open
        setTimeoutPromise(self._closeWindow, 0)
          // Open window
          .then(() => setTimeoutPromise(() => self._windowOpened = window.open(url), 0))
          // Close window
          .then(() => setTimeoutPromise(self._closeWindow, windowOpenInterval))
          // Store the data
          .then(() => setTimeoutPromise(() => callback(url), storeDataInterval))
          // Unlock and go to the next one
          .then(() => setTimeoutPromise(resolve, 0));

      });
    };
    self.crawl = function(callback, windowOpenInterval = 2000, storeDataInterval = 3000) {
      self._websites.reduce(function(currentPromise, nextUrl) {
        return currentPromise
            .then(() => self._openWebsite(nextUrl, callback, windowOpenInterval, storeDataInterval));
            //.then(() => delay(2000));
      }, Promise.resolve())
        .then(self._closeWindow);
    }
  };

  const Synchronizer = function(dbConnection) {
    const self = this;
    self._logger = new Logger();

    self._logger.debug('New synchronizer created!');
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
        const dataToStore = global.allConnections
          // Get only new connections (the last connections stored in global.allConnections)
          .slice(self._connectionsSent - global.allConnections.length)
          // Convert them to JSON objects
          .map((data) => self._arrayToJson(data))
          // Append the first party URL (the one given by lightbeam is based on heuristics)
          .map((data) => jQuery.extend(data, {
            firstParty: parseUri(url).host,
            crawlDate: moment().format('MM/DD/YYYY')
          }));

        // Store them into DB
        self._logger.debug('Storing ' + dataToStore.length + ' connecitons for first party ' + url);
        dataToStore.forEach((data) => self._dbConnection.store(data));

        self._connectionsSent = global.allConnections.length;
      } else {
        console.log('There are no new connections');
      }
    };

    //self._tempConnectionsSent = 0;
    //self._tempCheckNewConnections = function() {
    //  if (self._tempConnectionsSent < global.allConnections.length) {
    //    console.log('Found ' + (global.allConnections.length - self._tempConnectionsSent) + ' connections');
    //    const dataToStore = global.allConnections
    //        // Get only new connections (the last connections stored in global.allConnections)
    //        .slice(self._tempConnectionsSent - global.allConnections.length)
    //        // Convert them to JSON objects
    //        .map((data) => self._arrayToJson(data));
    //    // Append the first party URL (the one given by lightbeam is based on heuristics)
    //  //.map((data) => jQuery.extend(data, {firstParty: parseUri(url).host}));
    //
    //    // HERE!!!
    //    //console.log('NEW');
    //    //dataToStore.forEach(data => console.log(data.source));
    //
    //
    //    self._tempConnectionsSent = global.allConnections.length;
    //  } else {
    //    //console.log('There are no new connections');
    //  }
    //};
    //self.watch = () => setInterval(self._tempCheckNewConnections, 300);
  };

  const DbConnection = function() {
    const self = this;
    self._logger = null;
    self._logTable = 'log';
    self._firstPartyTable = 'first_parties';
    self._dataTable = 'data';
    self._database = 'myapp_test1';
    //self._host = '127.0.0.1';
    self._host = '192.33.93.94';
    self._port = 3000;
    console.log('New DB connection object created (' + self._host + ':' + self._port + ')! Make sure the mongod and mongodb-rest are running.');

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
          error: (xhr, status, error) => {console.log(error); reject(xhr, status, error);}
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

  const AnalysisUtils = function() {
    const self = this;
    self._urisMatch = function (uri1, uri2) {
      const host1 = parseUri(uri1).host;
      const host2 = parseUri(uri2).host;
      return host1.indexOf(host2) > -1 || host2.indexOf(host1) > -1;
    };
    self.isTp = (data) => !self._urisMatch(data.firstParty, data.target);
    //self.isFalseFp = (data) => self._isTp(data) && !self._isTpForLightbeam(data);
    self.isFalseFp = (data) => !self._urisMatch(data.firstParty, data.source);
    self.uniqueArray = function(array) {
      var seen = {};
      return array.filter(function(el) {
        if (seen[el]) return;
        seen[el] = true;
        return el;
      });
    }
  };

  const utils = new AnalysisUtils();
  const db = new DbConnection();
  db.clearLogs();
  db.clearData();
  const sync = new Synchronizer(db);
  db.getFirstParties().then(function(data) {
    new Crawler(data.map((record) => record.url))
      .crawl(sync.storeNewConnections, 5000);
  });

  //db.getFirstParties().then((data) => data.forEach(obj => console.log(parseUri(obj.url).host)));

  setTimeout(function() {
    console.log('CHECKING DATA-------------------');
    db.find().then(function(data) {
      const successes = utils.uniqueArray(data
        //.filter(object => utils.isTp(object))
        .filter(object => !utils._urisMatch(object.source, object.target))
        .map(object => object.target));
      const total = utils.uniqueArray(data
          .filter(object => utils.isTp(object))
          .map(object => object.target));
      const misses = utils.uniqueArray(data
        .filter(object => utils.isFalseFp(object))
        .map(object => object.target))
      console.log('successes: ' + successes);
      console.log('misses: ' + misses);
      console.log('total: ' + total);
      console.log('Difference: ' + (misses.length / total.length));

    });
  //  db.find().then(data => data
  //    .forEach(obj => {
  //    if (utils.isFalseFp(obj)) {
  //    console.log('FALSE -> '
  //      + parseUri(obj.firstParty).host
  //      + ' ' + parseUri(obj.target).host
  //      + ' ' + parseUri(obj.source).host);
  //  } else {
  //    //console.log('NOT FALSE -> '
  //    //  + parseUri(obj.firstParty).host
  //    //  + ' ' + parseUri(obj.target).host
  //    //  + ' ' + parseUri(obj.source).host);
  //  }
  //}));
  }, 20000);

  //db._find('first_parties', null, 'tracker').then((data) => data.forEach((record) => db._insert({url: record.url}, 'first_parties')));

})(this);
