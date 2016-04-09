/**
 * Created by alexandros on 4/7/16.
 */

import jQuery from 'jquery';

const defaultConfigs = {
  CACHE_LIFETIME: 10000,
  WINDOW_OPEN_INTERVAL: 5000,
  STORE_DATA_INTERVAL: 3000,

  //DATABASE_HOST: '127.0.0.1',
  DATABASE_HOST: '192.33.93.94',
  DATABASE_PORT: 3000,
  DATABASE_NAME: 'myapp_test1'

};

const customConfigs = {
  Alexandros: {
    DATABASE_NAME: 'alex_data'
  },
  Config2: {},
  Config3: {},
  default: {}
};

const ProfileConfigs = Object.keys(customConfigs)
  .reduce((configs, profile) => {
    configs[profile] = jQuery.extend({}, defaultConfigs, customConfigs[profile]);
    return configs;
  }, {});

export default ProfileConfigs;
