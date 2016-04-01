/**
 * Created by alexandros on 4/1/16.
 */

  import DbConnection from './DbConnection.js';

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

export default Logger;
