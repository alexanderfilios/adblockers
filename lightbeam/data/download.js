/**
 * Created by alexandros on 3/12/16.
 */
(function (global) {

  const Logger = global.AdblockerUtils.Logger;
  const DbConnection = global.AdblockerUtils.DbConnection;
  const Utilities = global.AdblockerUtils.Utilities;

  const Crawler = function(websites, config = {}) {
    const self = this;
    self._logger = new Logger();
    self._logger.debug('New Crawler created!');
    self._windowOpenInterval = isNaN(config.windowOpenInterval)
      ? Utilities.constants.WINDOW_OPEN_INTERVAL
      : parseInt(config.windowOpenInterval);
    self._storeDataInterval = isNaN(config.storeDataInterval)
      ? Utilities.constants.STORE_DATA_INTERVAL
      : parseInt(config.storeDataInterval);
    self._sampleFrom = isNaN(config.sampleFrom)
      ? 0
      : parseInt(config.sampleFrom);
    self._sampleUntil = isNaN(config.sampleUntil) || parseInt(config.sampleUntil) < self._sampleFrom
      ? websites.length
      : Math.min(websites.length, parseInt(config.sampleUntil));

    self._windowOpened = null;
    self._websites = websites.slice(self._sampleFrom, self._sampleUntil);
    self._closeWindow = function() {
      if (self._windowOpened !== null) {
        //console.log(self._windowOpened);
        self._logger.debug('Closing window');
        try {
          self._windowOpened.close();
        } catch (e) {
          console.log('Error closing the window.');
          console.log(e);
        }
        self._windowOpened = null;
      }
    };
    self._openWebsite = function(url, callback, windowOpenInterval, storeDataInterval) {
      return new Promise(function(resolve) {
        self._logger.debug('Open window ' + url);
        // Close window if open
        Utilities.setTimeoutPromise(self._closeWindow, 0)
          // Open window
          .then(() => Utilities.setTimeoutPromise(() => self._windowOpened = window.open(url), 0))
          // Close window
          .then(() => Utilities.setTimeoutPromise(self._closeWindow, windowOpenInterval))
          // Store the data
          .then(() => Utilities.setTimeoutPromise(() => callback(url), storeDataInterval))
          // Unlock and go to the next one
          .then(() => Utilities.setTimeoutPromise(resolve, 0));

      });
    };
    self.crawl = function(callback) {
      self._websites.reduce(function(currentPromise, nextUrl) {
        return currentPromise
            .then(() => self._openWebsite(nextUrl, callback, self._windowOpenInterval, self._storeDataInterval));
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
        fromPrivateMode: data[14],
        heuristics: data[15]
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
            crawlDate: moment().format(Utilities.constants.DATE_FORMAT)
          }));

        // Store them into DB
        self._logger.debug('Storing ' + dataToStore.length + ' connecitons for first party ' + url);
        dataToStore.forEach((data) => self._dbConnection.store(data));

        self._connectionsSent = global.allConnections.length;
      } else {
        console.log('There are no new connections');
      }
    };
  };

  ['profName', 'dbConnectionConfig', 'crawlerConfig'].forEach(settingId =>
      document.body.appendChild(Utilities.createElement(document, 'input', {
        type: 'hidden',
        id: settingId
      }))
  );


  function startCrawl(
    profile = 'default',
    dbConnectionConfigJson = '{a}',
    crawlerConfigJson = '{b}'
  ) {

    let dbConnectionConfig = {};
    let crawlerConfig = {};
    try {
      dbConnectionConfig = JSON.parse(dbConnectionConfigJson);
      crawlerConfig = JSON.parse(crawlerConfigJson);
    } catch (e) {}

    const db = new DbConnection(profile, dbConnectionConfig);
    const sync = new Synchronizer(db);

    // Clear data recorded today, in order to avoid double records for the same crawler
    db.clearData({crawlDate: moment().format(Utilities.constants.DATE_FORMAT)});
    db.getFirstParties().then(function (data) {

      new Crawler(data.map((record) => record.url), crawlerConfig)
        .crawl(sync.storeNewConnections);
    });
  }

  Utilities.repeatUntil(
    () => console.log(document.getElementById('profName').value),
    () => !!document.getElementById('profName').value,
    1000,
    () => startCrawl(
      document.getElementById('profName').value,
      document.getElementById('dbConnectionConfig').value,
      document.getElementById('crawlerConfig').value
    ));

})(this);
