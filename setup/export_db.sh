#!/usr/bin/env bash

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

export LC_ALL=C

dest_folder="$project_dir/backup/"$(date +"%Y-%m-%d")
mkdir -p $dest_folder
rm $dest_folder/*

for profile in ${profiles[@]}; do
  /usr/bin/mongoexport -d "myapp_test1" -c "data_$profile" -q "{crawlDate: \"$(date +%m/%d/%Y)\"}" -o "$dest_folder/data_$profile"
done
