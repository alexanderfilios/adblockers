#!/usr/bin/env bash

# Setup the cronjob (if not already set up) by running
# $ crontab -e
# Then append to the file the row
# 0 17 * * * path/to/project/setup/execute_crawl.sh

project_dir="$( dirname $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd ))"

step="50"
TOTAL_WEBSITES="1000"

sample_from="0"
sample_until=$(($sample_from + $step - 1))
window_open_interval="30000" # msec
store_data_interval="5000" # msec
rest_time="300" # sec

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

min() {
  echo $(($1 < $2 ? $1 : $2))
}

open_profiles() {
  for profile in ${profiles[@]}; do
    (cd "$project_dir/lightbeam"; export DISPLAY=:0; /usr/bin/jpm run -b /usr/bin/firefox -p "$profile"&)
  #  /usr/bin/firefox -p "$profile"&
    sleep 2 # Shift in time so that not all DB insertions fall together at the same time
  done
}

clear_data() {
  for profile in ${profiles[@]}; do
    "/usr/bin/mongo" "myapp_test1" "--eval" "db.data_$profile.remove({crawlDate: \"$(date +"%D")\"})"
  done
}

echo -e "Kill process executing:\n"\
  "\tkill -9 $$\n"\
  "\tkill all firefox\n"

echo -e "Clearing data recorded today..."
clear_data >/dev/null

while [ "$sample_from" -le "$((TOTAL_WEBSITES - 1))" ]; do

  crawled_websites=$(($sample_until - $sample_from + 1))
  estimated_time=$(($(($window_open_interval + $store_data_interval)) * $crawled_websites / 1000))
  total_wait_time=$(($estimated_time + $rest_time))
  current_time=$(date +"%T")

  echo -e "---------------------------------------------------------\n"\
    "Crawling from $sample_from to $sample_until\n"\
    "\tWebsites to crawl: $crawled_websites\n"\
    "\tEstimated time: $(($estimated_time / 60)) mins\n"\
    "\tTotal time until next crawl: $(($total_wait_time / 60)) mins\n"\
    "\tCurrent time: $current_time\n"

  echo -e "Killing Firefox instances and waiting 1 min..."
  killall firefox
  sleep 60

  echo -e "Setting Firefox config params..."
  "$project_dir/setup/setup_account.sh" "auto" $sample_from $sample_until $window_open_interval $store_data_interval

  echo -e "Start crawling and waiting..."
  open_profiles >/dev/null
  sleep $total_wait_time

  # Calculate for next round (if there is one)
  sample_from=$(($sample_from + $step))
  sample_until=$(min $(($sample_until + $step)) $(($TOTAL_WEBSITES - 1)))
done
