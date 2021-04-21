#!/bin/bash

STREAMLET_FOLDER=$1
if [ -z "$STREAMLET_FOLDER" ]; then
    echo "No streamlet folder specified."
    exit 1
fi

APPLICATION=$2
if [ -z "$APPLICATION" ]; then
    echo "No application name specified."
    exit 1
fi

cluster_id=$(jq -rc '.name' ${STREAMLET_FOLDER}streamlet.json | sed s'/\./\-/')

mkdir -p "${STREAMLET_FOLDER}output"
OUTPUT_CMD="${STREAMLET_FOLDER}output/delete-cmd.sh"

cat > "${OUTPUT_CMD}" << EOF
    kubectl delete sparkapplications.sparkoperator.k8s.io ${cluster_id} --namespace ${APPLICATION}
    kubectl wait --for=delete pods -l sparkoperator.k8s.io/app-name=${cluster_id} --namespace ${APPLICATION}
EOF
chmod a+x "${OUTPUT_CMD}"
