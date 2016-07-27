#!/bin/sh

export TOPOLOGY_JAR="target/LearnStorm-0.0.1-SNAPSHOT.jar"
export STORM_CMD="/bin/storm"	# HDP2.4.2
export TOPOLOGY_PACKAGE="com.pic.ala"
export TOPOLOGIES="ApLogGenerator ApLogAnalyzer LogAnalyzer"

for topology in $TOPOLOGIES
do
    $STORM_CMD jar $TOPOLOGY_JAR ${TOPOLOGY_PACKAGE}.${topology}
done

