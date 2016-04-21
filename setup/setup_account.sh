#!/usr/bin/env bash

# Creates a new account and sets the corresponding config variable profile.custom_name that is later used from the crawling instance
# ./setup_account.sh User1
# Check whether the result is correct by searching for profile.custom_name
# firefox -p User1 about:config

if [ $# -lt 1 ]; then
  echo "At least one parameter (username or \"auto\") has to be given!"
  exit 1
fi

# Set default parameters
sample_from="0"
sample_until="1000"
window_open_interval="20000"
store_data_interval="5000"
pref_file="user.js"

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
if [ ! -z "$2" ]; then
  sample_from="$2"
fi
if [ ! -z "$3" ]; then
  sample_until="$3"
fi
if [ ! -z "$4" ]; then
  window_open_interval="$4"
fi
if [ ! -z "$5" ]; then
  store_data_interval="$5"
fi
if [ ! -z "$6" ]; then
  pref_file="$6"
fi

echo -e "-------------------------------------\n"\
"Setting profiles with parameters for file $pref_file:\n"\
"\tCrawl range: $sample_from - $sample_until\n"\
"\tWindow open interval: $window_open_interval\n"\
"\tStore data interval: $store_data_interval\n"\
"-------------------------------------\n"

USER_AGENT="Mozilla/5.0 (iPhone; CPU iPhone OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5376e Safari/8536.25"

set_config_param() {
  profile="$1"
  param_name="$2"
  param_value="$3"
  new_param="user_pref(\"$param_name\", $param_value);"

  # Preferences file
  prefs=$(grep Path ~/.mozilla/firefox/profiles.ini | grep "$profile\$")
  prefs="$HOME/.mozilla/firefox/${prefs#Path=}/$pref_file"
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

create_profile() {
  profile="$1"

  if cat ~/.mozilla/firefox/profiles.ini | grep "Name=$profile\$"; then
    echo "Profile '$profile' exists!"
  else
    echo "Creating new profile for $profile..."
    firefox -CreateProfile "$profile"
  fi
}

for profile in ${profiles[@]}; do
  # Create the profile if it does not exist
  create_profile $profile >/dev/null

  # Set profile name
  set_config_param $profile "profile.custom_name" "\"$profile\"" >/dev/null
  # Set custom DbConnection params (optional)
  set_config_param $profile "profile.custom_db_connection_config" "\"{}\"" >/dev/null
  # Set custom Crawler params (optional)
  set_config_param $profile "profile.custom_crawler_config" "\"{\\\\\"sampleFrom\\\\\":\\\\\"$sample_from\\\\\",\\\\\"sampleUntil\\\\\":\\\\\"$sample_until\\\\\",\\\\\"windowOpenInterval\\\\\":\\\\\"$window_open_interval\\\\\",\\\\\"storeDataInterval\\\\\":\\\\\"$store_data_interval\\\\\"}\"" >/dev/null
  # Install unsigned XPIs (unpublished lightbeam)
  set_config_param $profile "xpinstall.signatures.required" "false" >/dev/null
  # Allow popups from add-ons (lightbeam opens popups to crawl them)
  set_config_param $profile "dom.disable_open_during_load" "false" >/dev/null

  # Set mobile user agent
  if [[ $profile == *"MUA" ]]; then
    set_config_param $profile "general.useragent.override" "\"$USER_AGENT\"" >/dev/null
  fi

done

echo "Done!"
