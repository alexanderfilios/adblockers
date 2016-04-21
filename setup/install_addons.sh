#!/usr/bin/env bash

# Installs the add-ons for each account, as defined in the profiles dictionary below.
# Installs are manual, i.e. the user is prompted to accept the installation.
# After installing, probably manual configurations are to follow (e.g. blacklists)

setup_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
project_dir="$(dirname $setup_dir)"
lightbeam_dir="$project_dir/lightbeam"

declare -A xpis=(
  ["adblockplus"]="$setup_dir/addons/{d10d0bf8-f5b5-c8b4-a8b2-2b9879e08c5d}.xpi"
  ["ghostery"]="$setup_dir/addons/ghostery-6.1.0-sm+fx.xpi"
  ["lightbeam"]="$lightbeam_dir/*.xpi"
)

declare -A profiles=(
  ["Ghostery_Default"]="ghostery"
  ["Ghostery_MaxProtection"]="ghostery"
  ["Adblockplus_Default"]="adblockplus"
  ["Adblockplus_MaxProtection"]="adblockplus"
  ["NoAdblocker"]=""
  ["NoAdblocker_DNT"]=""
  ["Ghostery_Default_MUA"]="ghostery"
  ["Ghostery_MaxProtection_MUA"]="ghostery"
  ["Adblockplus_Default_MUA"]="adblockplus"
  ["Adblockplus_MaxProtection_MUA"]="adblockplus"
  ["NoAdblocker_MUA"]=""
  ["NoAdblocker_DNT_MUA"]=""
)

killall firefox

for profile in ${!profiles[@]}; do
  echo "Profile: $profile -----------------------------------------_"
  IFS=" " read -a addons <<< "${profiles[$profile]}"
  for addon in ${addons[@]}; do
    echo "Installing addon $addon for $profile (XPI: ${xpis[$addon]})"
    firefox -p $profile ${xpis[$addon]}
  done
done
