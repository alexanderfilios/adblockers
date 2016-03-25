/**
 * Created by alexandros on 3/18/16.
 */

  import Constants from './Constants';

const Utilities = {
  constants: Constants,
  _isSubArrayOf: function(subArr, superArr) {
    const self = this;
    return subArr.every(k => superArr.some(n => self.areObjectsEqual(n, k)));
  },
  areObjectsEqual: function(obj1, obj2) {
    const self = this;
    if (typeof obj1 !== typeof obj2) {
      return false;
    } else if (typeof obj1 === 'string'
      || typeof obj1 === 'number'
      || typeof obj1 === 'boolean'
      || typeof obj1 === 'undefined'
      || obj1 === null) {
      return obj1 === obj2;
    } else if (Array.isArray(obj1)) {
      return Array.isArray(obj2)
        && self._isSubArrayOf(obj1, obj2)
        && self._isSubArrayOf(obj2, obj1);
    } else {
      return Object.keys(obj1)
        .every(k =>  self.areObjectsEqual(obj1[k], obj2[k]));
    }

  },
  repeatUntil: function (periodicCallback, condition, checkInterval, endCallback) {
    const intervalId = setInterval(function () {
      if (condition()) {
        clearInterval(intervalId);
        if (typeof endCallback === 'function') {
          endCallback();
        }
      } else {
        if (typeof periodicCallback === 'function') {
          periodicCallback();
        }
      }
    }, checkInterval);
  },

  repeatPromise: function (periodicCallback, condition, checkInterval) {
    const self = this;
    return new Promise((resolve) => self.repeatUntil(
      periodicCallback, condition, checkInterval, resolve));
  },

  setTimeoutPromise: function (timeout) {
    return new Promise((resolve) => setTimeout(resolve, timeout));
  },

  _uriOptions: {
    strictMode: false,
    key: ["source", "protocol", "authority", "userInfo", "user", "password", "host", "port", "relative", "path", "directory", "file", "query", "anchor"],
    q: {
      name: "queryKey",
      parser: /(?:^|&)([^&=]*)=?([^&]*)/g
    },
    parser: {
      strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
      loose: /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
    }
  },

  parseUri: function (str) {

    const self = this;
    var o = self._uriOptions,
      m = o.parser[o.strictMode ? "strict" : "loose"].exec(str),
      uri = {},
      i = 14;

    while (i--) uri[o.key[i]] = m[i] || "";

    uri[o.q.name] = {};
    uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
      if ($1) uri[o.q.name][$1] = $2;
    });

    return uri;


  },

  self: this,
  _urisMatch: function (uri1, uri2) {
    const self = this;
    const host1 = self.parseUri(uri1).host;
    const host2 = self.parseUri(uri2).host;

    return host1.indexOf(host2) > -1 || host2.indexOf(host1) > -1;
  },
  isTp: function (data) {
    const self = this;
    return !self._urisMatch(data.firstParty, data.target);
  },
  //self.isFalseFp = (data) => self._isTp(data) && !self._isTpForLightbeam(data);
  isFalseFp: function (data) {
    const self = this;
    return !self._urisMatch(data.firstParty, data.source);
  }
};
window.Utilities = Utilities;
export default Utilities;
