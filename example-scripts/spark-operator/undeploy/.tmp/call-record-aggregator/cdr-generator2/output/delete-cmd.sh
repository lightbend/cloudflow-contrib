    kubectl delete sparkapplications.sparkoperator.k8s.io call-record-aggregator-cdr-generator2 --namespace call-record-aggregator
    kubectl wait --for=delete pods -l sparkoperator.k8s.io/app-name=call-record-aggregator-cdr-generator2 --namespace call-record-aggregator
