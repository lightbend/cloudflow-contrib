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

SERVICE_ACCOUNT=$3
if [ -z "$SERVICE_ACCOUNT" ]; then
    echo "No service account specified."
    exit 1
fi

SAVEPOINT=$4
if [ -z "$SAVEPOINT" ]; then
    SAVEPOINT_STR=""
else
    SAVEPOINT_STR="--fromSavepoint=${SAVEPOINT}"
fi

cluster_id=$(jq -rc '.name' ${STREAMLET_FOLDER}streamlet.json | sed s'/\./\-/')
docker_image=$(jq -rc '.image' ${STREAMLET_FOLDER}streamlet.json)

mkdir -p "${STREAMLET_FOLDER}output"
OUTPUT_CMD="${STREAMLET_FOLDER}output/cli-cmd.sh"

cat > "${OUTPUT_CMD}" << EOF

    MASTER=\$(TERM=dumb kubectl cluster-info | grep master | sed -n -e 's/^.*at //p' | sed 's/\x1b\[[0-9;]*m//g')

    spark-submit \\
        --master "k8s://\${MASTER}" \\
        --deploy-mode cluster \\
        --name ${cluster_id} \\
        --class cloudflow.runner.Runner \\
        --conf spark.executor.instances=1 \\
        --conf spark.kubernetes.submission.waitAppCompletion=false \\
        --conf spark.kubernetes.namespace=${APPLICATION} \\
        --conf spark.kubernetes.driver.podTemplateFile=output/pod-template.yaml \\
        --conf spark.kubernetes.authenticate.driver.serviceAccountName=${SERVICE_ACCOUNT} \\
        --conf spark.streaming.stopGracefullyOnShutdown=true \\
        --conf spark.hadoop.fs.defaultFS=/mnt/storage/spark/${cluster_id} \\
        --conf spark.kubernetes.container.image=${docker_image} \\
        local:///opt/spark/work-dir/cloudflow-runner.jar
EOF
chmod a+x "${OUTPUT_CMD}"
