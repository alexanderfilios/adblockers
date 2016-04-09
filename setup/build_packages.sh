#!/usr/bin/env bash

# Builds the adblocker-utils node module and followingly the lightbeam and statistics projects. Useful after modifications in the adblocker-utils module, where all of the corresponding usages of the module have to be substituted in the rest of the projects.

project_dir="$( dirname $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd ))"

echo 'Building adblocker-utils...'
(cd "$project_dir/adblocker-utils/"; npm run build)
echo 'Building lightbeam...'
(cd "$project_dir/lightbeam/"; rm -r ./node_modules/adblocker-utils/; npm install; npm run build)
echo 'Building statistics...'
(cd "$project_dir/statistics/"; rm -r ./node_modules/adblocker-utils/; npm install)
