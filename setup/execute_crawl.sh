#!/usr/bin/env bash

# Setup the cronjob (if not already set up) by running
# $ crontab -e
# Then append to the file the row
# 0 17 * * * path/to/project/setup/execute_crawl.sh

project_dir="$( dirname $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd ))"

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

export DISPLAY=:0

for profile in ${profiles[@]}; do
#  (cd "$project_dir/lightbeam"; export DISPLAY=:0; /usr/local/bin/jpm run -b /usr/bin/firefox -p "$profile"&)
  /usr/bin/firefox -p "$profile"&
  sleep 2 # Shift in time so that not all DB insertions fall together at the same time
done
