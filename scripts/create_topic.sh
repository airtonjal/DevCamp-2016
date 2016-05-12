#!/bin/sh
kafka-topics --zookeeper localhost:2181 --create --topic tweets --partitions 10 --replication-factor 1
