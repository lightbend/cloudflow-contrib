#! /bin/bash

# Requisites:
# - bash
# - jq
# - kubectl (a recent one)
# - flink CLI on the PATH

APPLICATION=$1
if [ -z "$APPLICATION" ]; then
    echo "No application name specified."
    exit 1
fi

./fetch-streamlets.sh "${APPLICATION}"

./foreach-streamlet.sh "${APPLICATION}" ./generate-undeploy-cmd.sh

./foreach-streamlet.sh "${APPLICATION}" ./delete-streamlet.sh
