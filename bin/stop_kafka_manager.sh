#!/bin/sh

kill -9 $(cat /opt/kafka-manager-1.2.9.13/RUNNING_PID) && rm -f /opt/kafka-manager-1.2.9.13/RUNNING_PID
