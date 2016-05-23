/**
 * Created by alexandros on 5/23/16.
 */

var WebDriver = require('selenium-webdriver');
var FirefoxProfile = require('firefox-profile');

//GHOSTERY_DEFAULT_DIR = '/home/alexandros/.mozilla/firefox/4nlmtqqx.Ghostery_Default';
var profileDirectory = process.argv[2];

FirefoxProfile.copy({
  profileDirectory: profileDirectory,
  destinationDirectory: profileDirectory
}, function(error, profile) {
  //fp.setPreference('aaasomepreference.mypref', true);
  //fp.updatePreferences();

  profile.encoded(function(zippedProfile) {
    var browser = new WebDriver.Builder()
      .usingServer()
      .withCapabilities({
        'browserName': 'firefox',
        'firefox_profile': zippedProfile
      })
      .build();
    browser.get('https://extension.ghostery.com/settings')
    .then(function() {
        browser.findElements(WebDriver.By.css('#option-update-now'))
          .then(function(updateLinks) {
            updateLinks[0].click();
            setTimeout(function() {}, 10000);
          });
      })
  });
});




