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

runtime_config=$(jq -r 'paths(scalars) as $p | "        -D\($p|join("."))=\"\(getpath($p))\" \\"' ${STREAMLET_FOLDER}secrets/runtime-config.conf)

if [ -z "$runtime_config" ]; then
    runtime_config="\\"
fi

cat > "${OUTPUT_CMD}" << EOF
#!/bin/bash

    flink run-application \\
        --target kubernetes-application ${SAVEPOINT_STR} \\
        -Dkubernetes.cluster-id=${cluster_id} \\
        -Dkubernetes.service-account=${SERVICE_ACCOUNT} \\
        -Dkubernetes.container.image=${docker_image} \\
        -Dkubernetes.namespace=${APPLICATION} \\
        -Dhigh-availability=org.apache.flink.kubernetes.highavailability.KubernetesHaServicesFactory \\
        -Dhigh-availability.storageDir=file:///mnt/flink/storage/ha \\
        -Dstate.checkpoints.dir=file:///mnt/flink/storage/externalized-checkpoints/${cluster_id} \\
        -Dstate.backend.fs.checkpointdir=file:///mnt/flink/storage/checkpoints/${cluster_id} \\
        -Dstate.savepoints.dir=file:///mnt/flink/storage/savepoints/${cluster_id} \\
        -Dkubernetes.pod-template-file=output/pod-template.yaml \\
${runtime_config}
        local:///opt/flink/usrlib/cloudflow-runner.jar
EOF
chmod a+x "${OUTPUT_CMD}"
