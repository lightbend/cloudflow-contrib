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

secret_name=$(jq -rc '.secret_name' ${STREAMLET_FOLDER}streamlet.json)

mkdir -p "${STREAMLET_FOLDER}/output"

rm -rf "${STREAMLET_FOLDER}output/kubernetes"
cp -r "${PWD}/kubernetes" "${STREAMLET_FOLDER}output/kubernetes"

BASE_STAGE0="${STREAMLET_FOLDER}output/kubernetes/stage0/base-stage0.json"
STAGE0="${STREAMLET_FOLDER}output/kubernetes/stage0/stage0.json"

cluster_id=$(jq -rc '.name' ${STREAMLET_FOLDER}streamlet.json | sed s'/\./\-/')
docker_image=$(jq -rc '.image' ${STREAMLET_FOLDER}streamlet.json)

sed "s/  name: <CLUSTER_ID>/  name: ${cluster_id}/" "${STREAMLET_FOLDER}output/kubernetes/base/base-spark-cr.yaml" > "${STREAMLET_FOLDER}output/kubernetes/base/spark-cr.yaml"

pvc_name="not-exists"
pvc_claim_name="not-exists"
# find the attached PVC
jq -rc '.kubernetes.pods.pod.containers.container."volume-mounts" | keys[]' "${STREAMLET_FOLDER}secrets/pods-config.conf" | \
  while IFS='' read volume_name; do
    # echo "Volume name: $volume_name"

    is_pvc=$(jq -rc ".kubernetes.pods.pod.volumes.${volume_name}.pvc" "${STREAMLET_FOLDER}secrets/pods-config.conf")
    if [ -z $is_pvc ] || [ "$is_pvc" = "null" ] || [ "$is_pvc" = "" ]; then
      # Not a PVC
      true
    else
      pvc_name="$volume_name"
      pvc_claim_name=$(jq -r ".kubernetes.pods.pod.volumes.${volume_name}.pvc.name" "${STREAMLET_FOLDER}secrets/pods-config.conf")

      # Write the ouput file and exit
      # TODO improve especially error handling
      jq -r ".metadata.name = \"${cluster_id}\" | \
            .metadata.namespace = \"${APPLICATION}\" | \
            .spec.image = \"${docker_image}\" | \
            .spec.driver.secrets[0].name = \"${secret_name}\" | \
            .spec.executor.secrets[0].name = \"${secret_name}\" | \
            .spec.driver.serviceAccount = \"${SERVICE_ACCOUNT}\" | \
            .spec.driver.volumeMounts[0].name = \"${pvc_name}\" | \
            .spec.executor.volumeMounts[0].name = \"${pvc_name}\" | \
            .spec.volumes[0].name = \"${pvc_name}\" | \
            .spec.volumes[0].persistentVolumeClaim.claimName = \"${pvc_claim_name}\"" "${BASE_STAGE0}" \
        > "${STAGE0}"

      kubectl kustomize "${STREAMLET_FOLDER}output/kubernetes/stage0" > "${STREAMLET_FOLDER}output/cr.yaml"
    fi
  done

