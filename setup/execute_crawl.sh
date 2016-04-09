#!/usr/bin/env bash

# Setup the cronjob (if not already set up) by running
# $ crontab -e
# Then append to the file the row
# 0 17 * * * path/to/project/setup/execute_crawl.sh

project_dir="$( dirname $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd ))"

profiles=(
  "Alexandros"
)

for profile in ${profiles[@]}; do
  (cd "$project_dir/lightbeam"; export DISPLAY=:0; /usr/local/bin/jpm run -b /usr/bin/firefox -p "$profile")
done
