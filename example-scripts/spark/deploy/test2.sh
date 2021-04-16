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
  --conf spark.kubernetes.container.image=docker.io/andreatp/spark-aggregation@sha256:eb3152064f18409960b2e5ac36dc27668dffe6e4d5a81ecd555a34cab0cd0a8b \
  local:///opt/spark/work-dir/cloudflow-runner.jar
