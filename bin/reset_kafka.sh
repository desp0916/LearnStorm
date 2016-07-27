#!/bin/sh

source ${HOME}/bin/hadoop.sh

for host in $WORKER_NODES
do
  echo "Removing /hdp/hadoop/kafka/kafka-logs on ${host}..."
  ssh ${USER}@${host} rm -rf /hdp/hadoop/kafka/kafka-logs/{1,2}/*
done

/usr/bin/zookeeper-client <<EOF
rmr /LogAnalyzerV1
rmr /ApLogAnalyzerV1
rmr /admin
rmr /brokers
rmr /config
rmr /consumers
rmr /controller_epoch
rmr /kafka-manager
quit
EOF

