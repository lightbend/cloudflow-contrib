#!/bin/bash

APPLICATION=$1
if [ -z "$APPLICATION" ]; then
    echo "No application name specified."
    exit 1
fi

kubectl create serviceaccount flink-service-account --namespace "${APPLICATION}"
kubectl create clusterrolebinding flink-role-binding-flink --clusterrole=edit --serviceaccount="${APPLICATION}:flink-service-account"
