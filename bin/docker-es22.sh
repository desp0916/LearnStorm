#!/bin/sh

source ${HOME}/bin/hadoop.sh

ES_VERSION=2.2.1
ES_PATH=/root/docker/data/elasticsearch
ES_CONTAINER=ses

KB_VERSION=4.4.1
KB_CONTAINER=ski

start() {
  for host in $HADOOP_HOSTS
  do
    echo "Starting Elasticsearch ${ES_VERSION} on ${host} ...."
    ssh ${USER}@${host} docker run -d \
      -p 9200:9200 -p 9300:9300 \
      -v ${ES_PATH}/${ES_VERSION}/config:/usr/share/elasticsearch/config \
      -v ${ES_PATH}/usr/share/elasticsearch/data \
      --name ${ES_CONTAINER} desp0916/elasticsearch:${ES_VERSION}
    if [ "${host}" = hdp01 ]; then
      echo "Starting Kibana ${KB_VERSION} on ${host} ...."
      ssh ${USER}@${host} docker run -d \
        -p 5601:5601 --link ${ES_CONTAINER}:elasticsearch \
        --name ${KB_CONTAINER} kibana:${KB_VERSION}
    fi
  done
}

stop() {
  for host in $HADOOP_HOSTS
  do
    if [ "${host}" = hdp01 ]; then
      echo "Stoping Kibana ${KB_VERSION} on ${host} ...."
      ssh ${USER}@${host} docker rm -f "${KB_CONTAINER}"
    fi
    echo "Stoping Elasticsearch ${ES_VERSION} on ${host} ...."
    ssh ${USER}@${host} docker rm -f "${ES_CONTAINER}"
  done
}

case "$1" in
  start)
    $1
    ;;
  stop)
    $1
    ;;
  *)
   echo $"Usage: $0 {start|stop}"
    exit 2
esac

exit $?

