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
  profiles=("User1" "User2" "User3")
else
  profiles=("$1")
fi

for profile in ${profiles[@]}; do

  param_name="profile.custom_name"
  param_value="$profile"
  new_param="user_pref(\"$param_name\", \"$param_value\");"

  # Create the profile if it does not exist
  if cat ~/.mozilla/firefox/profiles.ini | grep "Name=$profile\$"; then
    echo "Profile '$profile' exists!"
  else
    echo "Creating new profile for $profile..."
    firefox -CreateProfile "$profile"
  fi

  # Preferences file
  prefs=$(grep Path ~/.mozilla/firefox/profiles.ini | grep "$profile")
  prefs="$HOME/.mozilla/firefox/${prefs#Path=}/prefs.js"
  if cat $prefs | grep $param_name; then
    # If parameter is already specified, replace it
    echo "Replacing user name config..."
    sed -i "/${param_name}/c\\$new_param" "$prefs"
  else
    # Otherwise, append a new line with the parameter
    echo "Adding user name config..."
    echo "$new_param" >> "$prefs"
  fi

  echo "Done!"

done
