#!/bin/sh

nohup /opt/kafka-manager-1.2.9.13/bin/kafka-manager -Dconfig.file=/opt/kafka-manager-1.2.9.13/conf/application.conf -java-home /usr/java/jdk1.8.0_65/ >/dev/null 2>&1 &

