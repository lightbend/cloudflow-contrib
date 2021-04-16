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
  --conf spark.kubernetes.container.image=docker.io/andreatp/spark-example:0.0.3 \
  local:///opt/spark/work-dir/cloudflow-runner_2.12-2.0.26-RC15.jar