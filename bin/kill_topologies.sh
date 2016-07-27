#!/bin/sh

export TOPOLOGIES="ApLogGeneratorV1 ApLogAnalyzerV1 LogAnalyzerV1"
export STORM_CMD="/bin/storm" # HDP 2.4.2
export WAIT=0

for topology in $TOPOLOGIES
do
    $STORM_CMD kill $topology -w $WAIT
done
