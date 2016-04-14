#!/usr/bin/env bash

setup_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
alexa_1m="$setup_dir/resources/top-1m.csv"
server_collection_url="http://127.0.0.1:3000/myapp_test1/first_parties2"

# Find the indices (ranks) that we want to save (top 500 and 500 random)
top_ranks=$(seq 1 500)
random_ranks=$(shuf -i 501-1000000 -n 500 | sort -n)
all_ranks=(${top_ranks[@]} ${random_ranks[@]})
#all_ranks=(1)
export IFS=","
for r in ${all_ranks[@]}; do
  sed "${r}q;d" $alexa_1m | while read rank url; do
   curl -d "{ \"rank\" : $rank, \"url\" : \"http://www.$url\" }" -H "Content-Type: application/json" $server_collection_url &> /dev/null
 done
done
