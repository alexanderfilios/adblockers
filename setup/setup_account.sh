#!/usr/bin/env bash

# Creates a new account and sets the corresponding config variable profile.custom_name that is later used from the crawling instance
# ./setup_account.sh User1
# Check whether the result is correct by searching for profile.custom_name
# firefox -p User1 about:config

if [ $# -ne 1 ]; then
  echo "Only one parameter (username or \"auto\") has to be given!"
  exit 1
fi

# Input
if [ "$1" == "auto" ]; then
  profiles=(
    "Ghostery_Default"
    "Ghostery_MaxProtection"
    "Adblockplus_Default"
    "Adblockplus_MaxProtection"
    "NoAdblocker"
    "NoAdblocker_DNT"
    "Ghostery_Default_MUA"
    "Ghostery_MaxProtection_MUA"
    "Adblockplus_Default_MUA"
    "Adblockplus_MaxProtection_MUA"
    "NoAdblocker_MUA"
    "NoAdblocker_DNT_MUA"
  )
else
  profiles=("$1")
fi

set_config_param() {
  profile="$1"
  param_name="$2"
  param_value="$3"
  new_param="user_pref(\"$param_name\", $param_value);"

  # Preferences file
  prefs=$(grep Path ~/.mozilla/firefox/profiles.ini | grep "$profile\$")
  prefs="$HOME/.mozilla/firefox/${prefs#Path=}/user.js"
  if cat $prefs | grep $param_name; then
    # If parameter is already specified, replace it
    echo "Replacing user name config: \"$param_name\" = \"$param_value\"..."
    sed -i "/${param_name}/c\\$new_param" "$prefs"
  else
    # Otherwise, append a new line with the parameter
    echo "Adding new config: \"$param_name\" = \"$param_value\"..."
    echo "$new_param" >> "$prefs"
  fi
}


for profile in ${profiles[@]}; do
  # Create the profile if it does not exist
  if cat ~/.mozilla/firefox/profiles.ini | grep "Name=$profile\$"; then
    echo "Profile '$profile' exists!"
  else
    echo "Creating new profile for $profile..."
    firefox -CreateProfile "$profile"
  fi

  # Set profile name
  set_config_param $profile "profile.custom_name" "\"$profile\""
  # Set custom DbConnection params (optional)
  set_config_param $profile "profile.custom_db_connection_config" "\"{}\""
  # Set custom Crawler params (optional)
  set_config_param $profile "profile.custom_crawler_config" "\"{\\\\\"sampleFrom\\\\\":\\\\\"0\\\\\",\\\\\"windowOpenInterval\\\\\":\\\\\"10000\\\\\"}\""
  # Install unsigned XPIs (unpublished lightbeam)
  set_config_param $profile "xpinstall.signatures.required" "false"
  # Allow popups from add-ons (lightbeam opens popups to crawl them)
  set_config_param $profile "dom.disable_open_during_load" "false"
  echo "Done!"
done
