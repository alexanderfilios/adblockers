#!/usr/bin/env bash


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

get_first_party_count() {
  profile="$1"
  response=$("/usr/bin/mongo" "myapp_test1" "--eval" "db.data_$profile.distinct('firstParty', {crawlDate: \"$(date +"%D")\"}).length")
  echo "$response" | grep '^[0-9]*$'
}

for profile in ${profiles[@]}; do
  echo -e "$profile\t\t\t" $(get_first_party_count $profile)
done
