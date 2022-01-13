#!/bin/bash

APPLICATION=$1
if [ -z "$APPLICATION" ]; then
    echo "No application name specified."
    exit 1
fi

kubectl create serviceaccount flink-service-account --namespace "${APPLICATION}"
# since this is an example the cluster-admin role is used for simplicity
# the 'edit' and 'admin' roles fail for configuration commands, since the service account needs to be able to watch configmaps
kubectl create clusterrolebinding flink-role-binding-flink --clusterrole=cluster-admin --serviceaccount="${APPLICATION}:flink-service-account"
