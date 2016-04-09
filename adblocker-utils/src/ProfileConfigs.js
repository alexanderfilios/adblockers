/**
 * Created by alexandros on 4/7/16.
 */

import jQuery from 'jquery';

const defaultConfigs = {
  DATABASE_HOST: '127.0.0.1',
  //DATABASE_HOST: '192.33.93.94',
  DATABASE_PORT: 3000,
  DATABASE_NAME: 'myapp_test1',
  DATA_COLLECTION: 'data'
};

const customConfigs = {
  Alexandros: {
    DATA_COLLECTION: 'data_Alexandros'
  },
  Ghostery_Default: {
    DATA_COLLECTION: 'data_Ghostery_Default'
  },
  Ghostery_MaxProtection: {
    DATA_COLLECTION: 'data_Ghostery_MaxProtection'
  },
  Adblockplus_Default: {
    DATA_COLLECTION: 'data_Adblockplus_Default'
  },
  Adblockplus_MaxProtection: {
    DATA_COLLECTION: 'data_Adblockplus_MaxProtection'
  },
  NoAdblocker: {
    DATA_COLLECTION: 'data_NoAdblocker'
  },
  NoAdblocker_DNT: {
    DATA_COLLECTION: 'data_NoAdblocker_DNT'
  },
  Ghostery_Default_MUA: {
    DATA_COLLECTION: 'data_Ghostery_Default_MUA'
  },
  Ghostery_MaxProtection_MUA: {
    DATA_COLLECTION: 'data_Ghostery_MaxProtection_MUA'
  },
  Adblockplus_Default_MUA: {
    DATA_COLLECTION: 'data_Adblockplus_Default_MUA'
  },
  Adblockplus_MaxProtection_MUA: {
    DATA_COLLECTION: 'data_Adblockplus_MaxProtection_MUA'
  },
  NoAdblocker_MUA: {
    DATA_COLLECTION: 'data_NoAdblocker_MUA'
  },
  NoAdblocker_DNT_MUA: {
    DATA_COLLECTION: 'data_NoAdblocker_DNT_MUA'
  },
  default: {}
};

const ProfileConfigs = Object.keys(customConfigs)
  .reduce((configs, profile) => {
    configs[profile] = jQuery.extend({}, defaultConfigs, customConfigs[profile]);
    return configs;
  }, {});

export default ProfileConfigs;
