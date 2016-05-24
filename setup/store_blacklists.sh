#!/usr/bin/env bash

setup_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
project_dir="$(dirname $setup_dir)"
date=$(date +"%Y-%m-%d")
dest_dir="$project_dir/blacklists/$date/"
dest_csv="$project_dir/paper/figures/data/blacklist-diff.csv"

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

sum(){ declare -i acc; for i; do acc+=i; done; echo $acc; }

store_list_ghostery() {
  dest_dir="$1"
  cp $(get_profile_path "Ghostery_Default") "$dest_dir"
}

store_list_adblockplus() {
  dst_name="$1"
  src_url="$2"
  curl -o "$dst_name" "$src_url"
}

update_list_adblockplus() {
  profile="$1"
  list_number="$2"
  # Open a unique instance of Firefox and focus on it
  killall firefox
  /usr/bin/firefox -p "$profile" & pid=$!
  sleep 3
#  WID=`xdotool search "Mozilla Firefox" | head -1`
  WID=`xdotool search --onlyvisible --pid $pid | head -1`
  xdotool windowactivate --sync $WID
  # Open the blacklist overview
  xdotool key --clearmodifiers ctrl+shift+e
  sleep 5
  # Start updating the lists
  for (( item=0; item<"$list_number"; item++ )); do
    xdotool key --clearmodifiers ctrl+t
    sleep 2
    xdotool key --clearmodifiers Down
  done
  kill $pid
}

update_list_ghostery() {
  profile="$1"
  # Profile directory
  profile_dir=$(grep Path ~/.mozilla/firefox/profiles.ini | grep "$profile\$")
  profile_dir="$HOME/.mozilla/firefox/${profile_dir#Path=}"
  /usr/local/bin/node "$project_dir/browser-tasks/index.js" "$profile_dir"
}

diff_txt() {
  prev="$1"
  curr="$2"
  diff=$(sdiff -B -b -s <(cat "$prev") <(cat "$curr") | wc -l)
  total=$(cat "$curr" | wc -l)
  res=$(bc -l <<< "$diff/$total")
  echo $res
}

diff_json() {
  prev="$1"
  curr="$2"
  diff=$(jsondiff "$prev" "$curr")
  words_changed=$(sum $(echo $diff | jq '.[] | if (.value | type == "array") then .value else [.value] end | length + 1'))
  total_words=$(grep -o '":"' "$curr" | wc -w)
  res=$(bc -l <<< "$words_changed/$total_words")
  echo $res
}

export DISPLAY=:0
export LC_ALL=C

########################################
# Update lists
########################################

update_list_adblockplus "Adblockplus_Default" 2
update_list_adblockplus "Adblockplus_MaxProtection" 5
#update_list_adblockplus "Adblockplus_Default_MUA" 2
#update_list_adblockplus "Adblockplus_MaxProtection_MUA" 5

update_list_ghostery "Ghostery_Default"
update_list_ghostery "Ghostery_MaxProtection"
#update_list_ghostery "Ghostery_Default_MUA"
#update_list_ghostery "Ghostery_MaxProtection_MUA"

########################################
# Store lists
########################################

mkdir -p "$dest_dir"

# Copy Ghostery list
store_list_ghostery "$dest_dir"

# Copy Adblockplus lists
for list in ${!lists[@]}; do
  store_list_adblockplus "$dest_dir/$list" "${lists[$list]}"
done

########################################
# Calculate diffs
########################################

> $dest_csv

echo "Date,Easylist,EasylistChina,Adservers,EasyPrivacy,Ghostery" >> $dest_csv

#min_date=$(date "+%Y-%m-%d")
#max_date=$(date -d "1970-01-01" "+%Y-%m-%d")
for curr_date in $(ls "$project_dir/blacklists"); do
#  if [[ $(date -d $curr_date "+%s") < $(date -d $min_date "+%s") ]];then
#    min_date=$curr_date
#  fi
#  if [[ $(date -d $curr_date "+%s") > $(date -d $max_date "+%s") ]];then
#    max_date=$curr_date
#  fi
  prev_date=$(date -d "$curr_date -1 day" "+%Y-%m-%d")
  if [[ -d "$project_dir/blacklists/$prev_date" ]];then
    vals=$(date -d $curr_date "+%m/%d/%Y")
    for list in ${!lists[@]}; do
      res=$(diff_txt "$project_dir/blacklists/$prev_date/$list" "$project_dir/blacklists/$curr_date/$list")
      vals="$vals,$res"
      echo "$curr_date: $list -> $res"
    done

    list="store.json"
    res=$(diff_json "$project_dir/blacklists/$prev_date/$list" "$project_dir/blacklists/$curr_date/$list")
    vals="$vals,$res"
    echo -e $vals >> $dest_csv
  fi
done
#echo $min_date " is min"
#echo $max_date " is max"
