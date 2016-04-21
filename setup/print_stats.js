/**
 * Created by alexandros on 4/21/16.
 */

var db = db.getSiblingDB('myapp_test1');
var date = new Date();
var formattedDate = ('0' + (date.getMonth() + 1)).substr(-2) + '/' + date.getDate() + '/' + date.getFullYear();

var profiles = [
  "Ghostery_Default",
  "Ghostery_MaxProtection",
  "Adblockplus_Default",
  "Adblockplus_MaxProtection",
  "NoAdblocker",
  "NoAdblocker_DNT",
  "Ghostery_Default_MUA",
  "Ghostery_MaxProtection_MUA",
  "Adblockplus_Default_MUA",
  "Adblockplus_MaxProtection_MUA",
  "NoAdblocker_MUA",
  "NoAdblocker_DNT_MUA"
];

for (var i in profiles) {
  print(profiles[i] + ': ' + db['data_' + profiles[i]].distinct('firstParty', {crawlDate: formattedDate}).length);
}
