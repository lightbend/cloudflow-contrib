
helm uninstall spark-operator --namespace cloudflow | true
kubectl delete job cloudflow-patch-spark-mutatingwebhookconfig -n cloudflow | true
helm repo add incubator https://charts.helm.sh/incubator | true
helm repo update
helm upgrade -i spark-operator incubator/sparkoperator --values="spark-values.yaml" --namespace cloudflow
kubectl apply -f spark-mutating-webhook.yaml --namespace cloudflow
