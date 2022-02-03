#!/bin/bash

APPLICATION=$1
if [ -z "$APPLICATION" ]; then
    echo "No application name specified."
    exit 1
fi

kubectl create serviceaccount flink-service-account --namespace "${APPLICATION}"
kubectl --namespace "${APPLICATION}" apply -f cluster-role.yaml

yq e ".subjects.[0].namespace = \"${APPLICATION}\"" cluster-role-binding.yaml > cluster-role-binding.tmp.yaml
kubectl --namespace "${APPLICATION}" apply -f cluster-role-binding.tmp.yaml
rm cluster-role-binding.tmp.yaml
