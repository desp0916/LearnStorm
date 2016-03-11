#!/bin/sh

source ${HOME}/bin/hadoop.sh

for host in $HADOOP_HOSTS
do
  ssh ${USER}@${host} rm -rf /hadoop/kafka-logs
done

/usr/bin/zookeeper-client <<EOF
rmr /aplog-analyzer
rmr /admin
rmr /brokers
rmr /config
rmr /consumers
rmr /controller_epoch
rmr /kafka-manager
quit
EOF

