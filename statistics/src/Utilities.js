/**
 * Created by alexandros on 3/18/16.
 */

const Utilities = {
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

export default Utilities;
