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
  ["user_agent_switcher"]="$setup_dir/addons/user_agent_switcher-0.7.3-fx+sm.xpi"
)

declare -A profiles=(
#  ["User1"]="lightbeam adblockplus"
#  ["User2"]="lightbeam ghostery"
  ["Alexandros"]="adblockplus lightbeam"
)

for profile in ${!profiles[@]}; do
  echo "Profile: $profile -----------------------------------------_"
  IFS=" " read -a addons <<< "${profiles[$profile]}"
  for addon in ${addons[@]}; do
    echo "Installing addon $addon for $profile (XPI: ${xpis[$addon]})"
    firefox -p $profile ${xpis[$addon]}
  done
done
