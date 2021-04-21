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

runtime_config=$(jq -r 'paths(scalars) as $p | "        --conf \($p|join("."))=\"\(getpath($p))\" \\"' ${STREAMLET_FOLDER}secrets/runtime-config.conf)

cat > "${OUTPUT_CMD}" << EOF
#!/bin/bash

    MASTER=\$(TERM=dumb kubectl cluster-info | grep 'Kubernetes control plane\|master' | sed -n -e 's/^.*at //p' | sed 's/\x1b\[[0-9;]*m//g')

    export SPARK_USER=185

    spark-submit \\
        --master "k8s://\${MASTER}" \\
        --deploy-mode cluster \\
        --name ${cluster_id} \\
        --class cloudflow.runner.Runner \\
        --conf spark.app.name=${cluster_id} \\
        --conf spark.kubernetes.container.image.pullPolicy=Always \\
        --conf spark.kubernetes.submission.waitAppCompletion=false \\
        --conf spark.kubernetes.namespace=${APPLICATION} \\
        --conf spark.kubernetes.driver.podTemplateFile=output/pod-template.yaml \\
        --conf spark.kubernetes.executor.podTemplateFile=output/pod-template.yaml \\
        --conf spark.kubernetes.authenticate.driver.serviceAccountName=${SERVICE_ACCOUNT} \\
        --conf spark.kubernetes.container.image=${docker_image} \\
${runtime_config}
        spark-internal
EOF
chmod a+x "${OUTPUT_CMD}"
