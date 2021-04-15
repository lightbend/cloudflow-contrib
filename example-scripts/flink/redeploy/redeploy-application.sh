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

SAVEPOINT=$(cd ../undeploy && ./undeploy-application.sh "${APPLICATION}")
echo "Savepoint is ${SAVEPOINT}"
(cd ../deploy && ./deploy-application.sh "${APPLICATION}" "${SERVICE_ACCOUNT}" "${SAVEPOINT}")
