#!/usr/bin/env bash

operation="count"
date=$(date +"%Y-%m-%d")

if [ ! -z "$1" ]; then
  operation="$1"
fi

if [ ! -z "$2" ]; then
  date=$(date -d $2 +"%Y-%m-%d")
fi

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

count() {
  profile="$1"
  date="$2"
  count=$(/usr/bin/mongo "myapp_test1" --eval "db.data_"$profile".count({crawlDate: '"$(date -d $date +"%m/%d/%Y")"'})" | grep "^[0-9]*$")
  echo $count
}
remove() {
  profile="$1"
  date="$2"
  $(/usr/bin/mongo "myapp_test1" --eval "db.data_"$profile".remove({crawlDate: '"$(date -d $date +"%m/%d/%Y")"'})" | grep "^[0-9]*$") >/dev/null
}

import() {
  profile="$1"
  date="$2"
  dest_folder="$project_dir/backup/"$date
  /usr/bin/mongoimport --db "myapp_test1" --collection "data_$profile" --file "$dest_folder/data_$profile"
}

count_first_parties() {
  profile="$1"
  date="$2"
  count=$(/usr/bin/mongo "myapp_test1" --eval "db.data_"$profile".distinct('firstParty', {crawlDate: '"$(date -d $date +"%m/%d/%Y")"'}).length" | grep "^[0-9]*$")
  echo $count
}

export() {
  profile="$1"
  date="$2"
  dest_folder="$project_dir/backup/"$date
  dest_file="$dest_folder/data_$profile"
  mkdir -p "$dest_folder"
  rm $dest_file >/dev/null
  /usr/bin/mongoexport -d "myapp_test1" -c "data_$profile" -q "{crawlDate: '"$(date -d $date +"%m/%d/%Y")"'}" -o "$dest_file"
}
for profile in ${profiles[@]}; do
  if [ "$operation" = "count" ]; then
    echo -e "$profile:\t"$(count $profile $date)
  elif [ "$operation" = "count_first_parties" ]; then
    echo -e "$profile:\t"$(count_first_parties $profile $date)
  elif [ "$operation" = "clean" ]; then
    remove $profile $date
  elif [ "$operation" = "import" ]; then
    import $profile $date
  elif [ "$operation" = "clean_import" ]; then
    remove $profile $date
    import $profile $date
  elif [ "$operation" = "export" ]; then
    export $profile $date
  fi
done

