#!/bin/bash

if [ -z "${TARGET_URL}" ]; then
    echo 'The required "TARGET_URL" environment variable is not set'
    exit 1
fi

if [ -z "${DELAY_BEFORE_LOAD}" ]; then
    DELAY_BEFORE_LOAD='60s'
fi

echo "Waiting ${DELAY_BEFORE_LOAD} before commencing the barrage"

sleep "${DELAY_BEFORE_LOAD}"

echo "Commencing the barrage!"

request_count=0

while true
do
    curl --silent --show-error --url "${TARGET_URL}" > /dev/null

    request_count=$((request_count+1))

    if [ -n "${LOG_REQUEST_COUNT}" ]; then
        if [ $((request_count%1000)) = 0 ]; then
            echo "${request_count} requests issued so far"
        fi
    fi
done