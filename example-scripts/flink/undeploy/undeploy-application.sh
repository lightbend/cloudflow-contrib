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

../../common/fetch-streamlets.sh "${APPLICATION}" flink

../../common/foreach-streamlet.sh "${APPLICATION}" ./generate-undeploy-cmd.sh

../../common/foreach-streamlet.sh "${APPLICATION}" ./delete-streamlet.sh
