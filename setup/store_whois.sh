#!/usr/bin/env bash

project_dir="$( dirname $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd ))"

#domains=($("$project_dir/setup/execute_db_script.sh" "get_first_parties") $("$project_dir/setup/execute_db_script.sh" "get_third_parties"))
#domains=($("$project_dir/setup/execute_db_script.sh" "get_first_parties"))
domains=($("$project_dir/setup/execute_db_script.sh" "get_third_parties"))

declare -A matcher_mapping=(
  ["regis_org"]="Registrant Organization"
  ["regis_city"]="Registrant City;City"
  ["regis_country"]="Registrant Country;Country"
  ["regis_street"]="Registrant Street;Registrant Address;Registrant-address;Address"
  ["admin_org"]="Admin-name;Admin Organization"
  ["admin_city"]="Admin City;City"
  ["admin_street"]="Admin Street;Admin Address;Admin-address;Address"
  ["admin_country"]="Admin Country;Country"
  ["tech_org"]="Tech-name;Tech Organization"
  ["tech_city"]="Tech City;City"
  ["tech_street"]="Tech Street;Tech Address;Tech-address;Address"
  ["tech_country"]="Tech Country;Country"
  ["created"]="Creation;Created On"
  ["last_updated"]="Updated;Last Updated On;Update"
  ["exp_date"]="Expiration Date;Valid"
)


upsert() {
  collection="$1"
  query="$2"
  json_object="$3"
  /usr/bin/mongo "myapp_test1" --eval "db."$collection".update("$query","$json_object",{upsert: true})"
}

get_ip() {
  url="$1"
  domain=${url#http://www.}
  ip=$(dig "$domain" +noall +answer | grep "IN\sA" | head -n 1 | awk '{print $5}')
  if [[ -z $ip ]];then
    domain=${url#http://}
    ip=$(dig "$domain" +noall +answer | grep "IN\sA" | head -n 1 | awk '{print $5}')
  fi
  echo "$ip"
}

get_country_code() {
  ip="$1"
  geoiplookup "$ip" | cut -f1 -d',' | awk '{print $4}'
}

if [[ $1 == "whois" ]];then

for domain in ${domains[@]}; do
#  domain=${domain#http://www.}
  whois=$(/usr/bin/whois "$domain")
  echo "------------------------------------------"
  echo "Getting data for domain: $domain"
  json_object="{"
  for key in ${!matcher_mapping[@]}; do
    value=""
    while read matcher; do
      value=$(echo "$whois" | grep -i "$matcher" | cut -d ':' -f 2 | awk '{$1=$1};1' | head -n 1)
#      match_row=$(echo "$whois" | grep -i "$matcher")
#      value=$(echo "$match_row" | cut -d ':' -f 2 | cut -d '\t' -f 2 | awk '{$1=$1};1' | head -n 1)
#      field_name=$(echo "$match_row" | cut -d ':' -f 1 | cut -d '\t' -f 1 | awk '{$1=$1};1' | head -n 1)
      # If we found it, no reason to look further. Store the result and stop looking
      if [ "$value" != "" ]; then
        echo -e "$key:\t\t$value ($matcher)"
        esc_value=$(echo "$value" | sed -e 's/\s/_/g')
        json_object=$json_object"\"$key\":\"$esc_value\","
        break;
      fi
    done < <(echo "${matcher_mapping[$key]}" | sed -n 1'p' | tr ';' '\n')
  done
  json_object=$json_object"\"domain\":\"$domain\"}"
  echo $json_object
  upsert "entity_details" "{'domain':'$domain'}" "$json_object"
  echo "------------------------------------------"
done

elif [[ $1 == "dns" ]];then

for domain in ${domains[@]}; do
#  echo "------------------------------------------"
#  echo "Domain: $domain"
#  domain=${domain#http://www.}
#  whois=$(/usr/bin/whois "$domain")
  ip=$(get_ip "$domain")


  if [[ -z $ip ]];then
    echo -e "\t\t\tDid not find $domain"
  else
    country_code=$(get_country_code "$ip")
#    country_code="aa"
#echo $country_code
    echo -e "$ip\t$country_code\t$domain"
    domain=${domain#http://www.}

    json_object="{\"ip\":\"$ip\",\"country\":\"$country_code\",\"domain\":\"$domain\"}"
    upsert "third_entity_countries" "{'domain':'$domain'}" "$json_object"
  fi

#  for key in ${!matcher_mapping[@]}; do
#    value=""
#    while read matcher; do
#      value=$(echo "$whois" | grep -i "$matcher" | cut -d ':' -f 2 | awk '{$1=$1};1' | head -n 1)
##      match_row=$(echo "$whois" | grep -i "$matcher")
##      value=$(echo "$match_row" | cut -d ':' -f 2 | cut -d '\t' -f 2 | awk '{$1=$1};1' | head -n 1)
##      field_name=$(echo "$match_row" | cut -d ':' -f 1 | cut -d '\t' -f 1 | awk '{$1=$1};1' | head -n 1)
#      # If we found it, no reason to look further. Store the result and stop looking
#      if [ "$value" != "" ]; then
#        echo -e "$key:\t\t$value ($matcher)"
#        esc_value=$(echo "$value" | sed -e 's/\s/_/g')
#        json_object=$json_object"\"$key\":\"$esc_value\","
#        break;
#      fi
#    done < <(echo "${matcher_mapping[$key]}" | sed -n 1'p' | tr ';' '\n')
#  done

#  echo $json_object
#  upsert "entity_detailss" "{'domain':'$domain'}" "$json_object"
#  echo "------------------------------------------"
done

fi
