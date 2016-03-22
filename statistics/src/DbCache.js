/**
 * Created by alexandros on 3/22/16.
 */

const DbCache = function(lifetime = 10000) {
  const self = this;
  self._lifetime = lifetime;
  self._cache = [];

  self.clear = function(collection, filter) {
    self._cache = self._cache.filter(entry => entry.collection !== collection || entry.filter !== filter);
  };
  self.get = function(collection, filter) {
    const cacheData = self._cache
      .filter(entry => entry.collection === collection && entry.filter === filter);

    if (cacheData.length > 0 && (new Date() - cacheData[0].lastFetched <= self._lifetime)) {
      return cacheData[0];
    } else {
      return null;
    }
  };
  self.put = function(collection, filter, data) {
    self.clear(collection, filter);
    self._cache.push({
      collection: collection,
      filter: filter,
      data: data,
      lastFetched: new Date()
    });
  };
};

export default DbCache;
