#!/usr/bin/env bash

echo 'Building adblocker-utils...'
(cd ./adblocker-utils/; npm run build)
echo 'Building lightbeam...'
(cd ./lightbeam/; rm -r ./node_modules/adblocker-utils/; npm install)
echo 'Building statistics...'
(cd ./statistics/; rm -r ./node_modules/adblocker-utils/; npm install)

#if [ -a /var/spool/cron/alexandros ]; then
#  rm /var/spool/cron/alexandros
#fi
#
##cp ./alexandros /var/spool/cron/
#CRON_TASK="* * * * * /usr/bin/firefox"
#
##(crontab -l 2>/dev/null; echo ) | crontab -
#
#if crontab -l | grep -q "$CRON_TASK"; then
#  echo "Cronjob exists!"
#else
#  echo "sudo crontab -e"
#  echo "$CRON_TASK"
##  printf "\nA new line" >> ./alexandros
#fi
