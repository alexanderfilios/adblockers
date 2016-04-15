/**
 * Created by alexandros on 4/7/16.
 */

import jQuery from 'jquery';

const defaultConfigs = {
  host: '127.0.0.1',
  //host: '192.33.93.94',
  port: 3000,
  database: 'myapp_test1',
  firstPartiesTable: 'first_parties',
  dataTable: 'data'
};

const customConfigs = {
  Alexandros: {
    dataTable: 'data_Alexandros'
  },
  Ghostery_Default: {
    dataTable: 'data_Ghostery_Default'
  },
  Ghostery_MaxProtection: {
    dataTable: 'data_Ghostery_MaxProtection'
  },
  Adblockplus_Default: {
    dataTable: 'data_Adblockplus_Default'
  },
  Adblockplus_MaxProtection: {
    dataTable: 'data_Adblockplus_MaxProtection'
  },
  NoAdblocker: {
    dataTable: 'data_NoAdblocker'
  },
  NoAdblocker_DNT: {
    dataTable: 'data_NoAdblocker_DNT'
  },
  Ghostery_Default_MUA: {
    dataTable: 'data_Ghostery_Default_MUA'
  },
  Ghostery_MaxProtection_MUA: {
    dataTable: 'data_Ghostery_MaxProtection_MUA'
  },
  Adblockplus_Default_MUA: {
    dataTable: 'data_Adblockplus_Default_MUA'
  },
  Adblockplus_MaxProtection_MUA: {
    dataTable: 'data_Adblockplus_MaxProtection_MUA'
  },
  NoAdblocker_MUA: {
    dataTable: 'data_NoAdblocker_MUA'
  },
  NoAdblocker_DNT_MUA: {
    dataTable: 'data_NoAdblocker_DNT_MUA'
  },
  default: {}
};

const ProfileConfigs = Object.keys(customConfigs)
  .reduce((configs, profile) => {
    configs[profile] = jQuery.extend({}, defaultConfigs, customConfigs[profile]);
    return configs;
  }, {});

export default ProfileConfigs;
