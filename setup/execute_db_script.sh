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

get_first_parties() {
  /usr/bin/mongo "myapp_test1" --eval "cursor = db.first_parties.find({}, {'url': 1, '_id': 0}); while (cursor.hasNext()) print(cursor.next().url);" | sed 1,2d
}

get_third_parties() {
  declare -a unique
#  unique=(zqtk.net zoznam.sk zotabox.com zorginstituutnederland.nl)
#unique=(google.com)
  for profile in ${profiles[@]}; do
    current=($(/usr/bin/mongo "myapp_test1" --eval "array = db.data_"$profile".distinct('target'); for (i in array) print(array[i]);" | sed 1,2d))
    unique=("${unique[@]}" "${current[@]}")
    unique=($(printf "%s\n" "${unique[@]}" | sort | uniq -c | sort -rnk1 | awk '{ print $2 }'))
  done
  for third_party in ${unique[@]}; do echo $third_party; done
}

count_third_parties() {
  profile="$1"
  date="$2"
  count=$(/usr/bin/mongo "myapp_test1" --eval "db.data_"$profile".distinct('target', {crawlDate: '"$(date -d $date +"%m/%d/%Y")"'}).length" | grep "^[0-9]*$")
  echo $count
}

export_db() {
  profile="$1"
  date="$2"
  dest_folder="$project_dir/backup/"$date
  dest_file="$dest_folder/data_$profile"
  mkdir -p "$dest_folder"
  rm $dest_file >/dev/null
  /usr/bin/mongoexport -d "myapp_test1" -c "data_$profile" -q "{crawlDate: '"$(date -d $date +"%m/%d/%Y")"'}" -o "$dest_file"
}

if [ "$operation" = "get_first_parties" ]; then
  get_first_parties
  exit 0
elif [ "$operation" = "get_third_parties" ]; then
  get_third_parties
  exit 0
fi

for profile in ${profiles[@]}; do
  if [ "$operation" = "count" ]; then
    echo -e "$profile:\t"$(count $profile $date)
  elif [ "$operation" = "count_first_parties" ]; then
    echo -e "$profile:\t"$(count_first_parties $profile $date)
  elif [ "$operation" = "clean" ]; then
    remove $profile $date
  elif [ "$operation" = "import" ]; then
    import $profile $date
  elif [ "$operation" = "count_third_parties" ]; then
    echo -e "$profile:\t"$(count_third_parties $profile $date)
  elif [ "$operation" = "clean_import" ]; then
    remove $profile $date
    import $profile $date
  elif [ "$operation" = "export" ]; then
    export_db $profile $date
  fi
done

