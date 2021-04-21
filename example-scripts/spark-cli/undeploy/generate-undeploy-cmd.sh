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

# NOTE
# we need to cleanup the ConfigMaps manually as reported:
# https://issues.apache.org/jira/browse/FLINK-22262
#
cat > "${OUTPUT_CMD}" << EOF
#!/bin/bash

    MASTER=\$(TERM=dumb kubectl cluster-info | grep 'Kubernetes control plane\|master' | sed -n -e 's/^.*at //p' | sed 's/\x1b\[[0-9;]*m//g')

    spark-submit --kill ${APPLICATION}:${cluster_id}* --master "k8s://\${MASTER}" --conf spark.kubernetes.appKillPodDeletionGracePeriod=60
EOF
chmod a+x "${OUTPUT_CMD}"
