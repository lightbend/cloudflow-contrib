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

SERVICE_ACCOUNT=$2
if [ -z "$SERVICE_ACCOUNT" ]; then
    echo "No service account specified."
    exit 1
fi

SAVEPOINT=$3

../../common/fetch-streamlets.sh "${APPLICATION}" spark

../../common/foreach-streamlet.sh "${APPLICATION}" ./generate-cli-cmd.sh "${SERVICE_ACCOUNT}" "${SAVEPOINT}"
../../common/foreach-streamlet.sh "${APPLICATION}" ./generate-pod-template.sh

../../common/foreach-streamlet.sh "${APPLICATION}" ./create-streamlet.sh
