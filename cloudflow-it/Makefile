  ifeq ($(version),)
    $(error variable 'version' must be manually specified when running a release task)
  endif

MACHINE_ID ?= $(shell sh -c "system_profiler SPHardwareDataType | grep 'Serial Number (system)' | sed 's/Serial Number (system): //' | sed 's/ *//g' | tr '[:upper:]' '[:lower:]'")
DOCKER_REGISTRY ?= "docker.io"
DOCKER_REPOSITORY ?= "lightbend"

.PHONY: all
all: | spawn-gke-cluster prepare-swiss-knife prepare-cluster run-it-tests delete-gke-cluster

.PHONY: on-cluster
on-cluster: | prepare-swiss-knife prepare-cluster run-it-tests

.PHONY: prepare-swiss-knife
prepare-swiss-knife:
	@echo '****** Prepare and publish the swiss-knife application'
	(cd swiss-knife && \
		CLOUDFLOW_CONTRIB_VERSION=${version}  \
			sbt -mem 2048 \
				'set version in ThisBuild := "${version}"' \
				'set cloudflowDockerRegistry in ThisBuild := Some("${DOCKER_REGISTRY}")' \
				'set cloudflowDockerRepository in ThisBuild := Some("${DOCKER_REPOSITORY}")' \
				clean buildApp)
	@echo '****** Copy the cr file to the itests relevant folder'
	(cp swiss-knife/target/swiss-knife.json src/it/resources/)

.PHONY: prepare-clis
prepare-clis:
	@echo '****** Prepare the runtimes Clis'
	wget https://downloads.apache.org/spark/spark-3.1.2/spark-3.1.2-bin-hadoop2.7.tgz
	tar -xf spark-3.1.2-bin-hadoop2.7.tgz
	mv spark-3.1.2-bin-hadoop2.7 spark
	rm spark-3.1.2-bin-hadoop2.7.tgz
	wget https://downloads.apache.org/flink/flink-1.13.2/flink-1.13.2-bin-scala_2.12.tgz
	tar -xf flink-1.13.2-bin-scala_2.12.tgz
	mv flink-1.13.2 flink
	rm flink-1.13.2-bin-scala_2.12.tgz

.PHONY: prepare-cluster
prepare-cluster:
	@echo '****** Cluster setup, kubectl needs to be configured'
	@echo '****** Creating namespace'
	kubectl create ns cloudflow | true
	@echo '****** Installing Kafka'
	helm repo add strimzi https://strimzi.io/charts/ | true
	helm repo update
	helm upgrade -i strimzi strimzi/strimzi-kafka-operator --namespace cloudflow
	(for i in 1 2 3; do kubectl apply -f kafka-cluster.yaml && break || sleep 2; done)
	@echo '****** Installing NFS provisioner'
	helm repo add stable https://charts.helm.sh/stable --force-update | true
	helm repo update
	helm upgrade -i nfs-server-provisioner stable/nfs-server-provisioner --set storageClass.provisionerName=cloudflow-nfs --namespace cloudflow
	@echo '****** Installing Cloudflow Operator'
	helm repo add cloudflow-helm-charts https://lightbend.github.io/cloudflow-helm-charts/ | true
	helm repo update
	(helm upgrade -i cloudflow cloudflow-helm-charts/cloudflow \
		--atomic \
		--version "${version}" \
		--set cloudflow_operator.jvm.opts="-XX:MaxRAMPercentage=90.0 -XX:+UseContainerSupport -Dcloudflow.platform.flink-enabled=false -Dcloudflow.platform.spark-enabled=false" \
		--set kafkaClusters.default.bootstrapServers=cloudflow-strimzi-kafka-bootstrap.cloudflow:9092 \
		--namespace cloudflow)
	@echo '****** The cluster is ready for integration tests!'

.PHONY: spawn-gke-cluster
spawn-gke-cluster:
	@echo '****** Spawn a GKE cluster to run the IT tests on'
	(bash -c "source ./create-cluster-gke.sh it-${MACHINE_ID}")

.PHONY: delete-gke-cluster
delete-gke-cluster:
	@echo '****** Deleting your GKE cluster'
	(gcloud container clusters delete --quiet "it-${MACHINE_ID}" | true && \
	  gcloud compute disks list --format="table[no-heading](name)" --filter="name~^gke-it-${MACHINE_ID}" | xargs -n1 gcloud compute disks delete --quiet)

.PHONY: run-it-tests
run-it-tests:
	@echo '****** Run Integration Tests'
	(cd .. && \
		sbt cloudflow-it/it:test)
