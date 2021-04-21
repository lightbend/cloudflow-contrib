#!/bin/bash

APPLICATION=$1
if [ -z "$APPLICATION" ]; then
    echo "No application name specified."
    exit 1
fi

kubectl create serviceaccount spark-service-account --namespace "${APPLICATION}"
kubectl create clusterrolebinding spark-role-binding-spark --clusterrole=edit --serviceaccount="${APPLICATION}:spark-service-account"
