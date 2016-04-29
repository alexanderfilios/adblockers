#!/usr/bin/env bash

project_dir="$( dirname $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd ))"

domains=($("$project_dir/setup/execute_db_script.sh" "get_first_parties"))

matcher="Location: "

upsert() {
  collection="$1"
  query="$2"
  json_object="$3"
  /usr/bin/mongo "myapp_test1" --eval "db."$collection".update("$query","$json_object",{upsert: true})"
}

for domain in ${domains[@]}; do
  curl=$(/usr/bin/curl -X HEAD -i "$domain" --max-time 2 2>/dev/null)
  echo "------------------------------------------"
  echo "Getting data for domain: $domain"
  value=$(echo "$curl" | grep -i "$matcher")
  value=$(echo ${value#"$matcher"} | tr -d '\r' | awk '{$1=$1};1' | head -n 1)
  # If we found it, no reason to look further. Store the result and stop looking
  if [ "$value" == "" ]; then
    value="$domain"
  fi

  json_object="{'original_url':'$domain','actual_url':'$value'}"
  echo "Stroring: $json_object"
  upsert "redirection_mapping" "{'original_url':'$domain'}" "$json_object"
done
