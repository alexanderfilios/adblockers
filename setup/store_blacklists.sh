#!/usr/bin/env bash

setup_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
project_dir="$(dirname $setup_dir)"
date=$(date +"%Y-%m-%d")
dest_dir="$project_dir/blacklists/$date/"

get_profile_path() {
  profile="$1"
  prefs=$(grep Path ~/.mozilla/firefox/profiles.ini | grep "$profile\$")
  path="$HOME/.mozilla/firefox/${prefs#Path=}/jetpack/firefox@ghostery.com/simple-storage/store.json"
  echo "$path"
}

declare -A lists=(
  ["easylist.txt"]="https://easylist-downloads.adblockplus.org/easylist.txt"
  ["easylistchina+easylist.txt"]="https://easylist-downloads.adblockplus.org/easylistchina+easylist.txt"
  ["adservers.txt"]="http://pgl.yoyo.org/adservers/serverlist.php?hostformat=adblockplus&mimetype=plaintext"
  ["easyprivacy.txt"]="https://easylist-downloads.adblockplus.org/easyprivacy.txt"
)

mkdir -p "$dest_dir"

# Copy Ghostery list
cp $(get_profile_path "Ghostery_Default") "$dest_dir"

# Copy Adblockplus lists
for list in ${!lists[@]}; do
  curl -o "$dest_dir/$list" "${lists[$list]}"
done
