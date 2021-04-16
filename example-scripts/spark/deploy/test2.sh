MASTER=$(TERM=dumb kubectl cluster-info | grep master | sed -n -e 's/^.*at //p' | sed 's/\x1b\[[0-9;]*m//g')

spark-submit \
  --master "k8s://${MASTER}" \
  --deploy-mode cluster \
  --name call-record-aggregator-cdr-generator1 \
  --class cloudflow.runner.Runner \
  --conf spark.executor.instances=1 \
  --conf spark.kubernetes.submission.waitAppCompletion=false \
  --conf spark.kubernetes.namespace=call-record-aggregator \
  --conf spark.kubernetes.driver.podTemplateFile=pod-template2.yaml \
  --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark-service-account \
  --conf spark.kubernetes.container.image=docker.io/andreatp/spark-aggregation@sha256:2c09a222b094ffff7b01a04ae5cbbee011b4f25199062da3674ad57c444609dd \
  local:///opt/spark/work-dir/cloudflow-runner.jar
