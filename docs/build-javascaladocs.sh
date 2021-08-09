#! /bin/bash

VERSION=$1

echo $VERSION
DIR=$(echo ${VERSION} | sed 's/v//')

if [[ -z "${VERSION}" ]]; then
  echo "Version not defined"
else
  (cd target && \
    rm -rf cloudflow-contrib && \
    git clone --depth=100 --branch "$VERSION" https://github.com/lightbend/cloudflow-contrib.git && \
    cd cloudflow-contrib && \
    (sbt "clean; flink-docs/unidoc; spark-docs/unidoc" || true) && \
    cd ../../ && \
    mkdir -p ./target/docs/$DIR/api/spark-scaladoc && \
    mkdir -p ./target/docs/$DIR/api/flink-scaladoc && \
    cp -r "./target/cloudflow-contrib/spark-docs/target/scala-2.12/unidoc" "./target/docs/$DIR/api/spark-scaladoc" && \
    cp -r "./target/cloudflow-contrib/flink-docs/target/scala-2.12/unidoc" "./target/docs/$DIR/api/flink-scaladoc" && \
    rm -rf ./target/cloudflow-contrib)
fi
