#Kafka Performance Test

<https://gist.github.com/jkreps/c7ddb4041ef62a900e6c>

## 1. Producer

### 1.2 Setup

``` sh
bin/kafka-topics.sh --zookeeper hdpr01hn01:2181 --create --topic test-rep-one --partitions 10 --replication-factor 1
bin/kafka-topics.sh --zookeeper hdpr01hn01:2181 --create --topic test --partitions 10 --replication-factor 3
```

### 1.3 Single thread, no replication

``` sh
bin/kafka-producer-perf-test.sh --topics test-rep-one --broker-list hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 --request-num-acks 1 --threads 1 --message-size 100 --messages 50000000 --batch-size 8196 --sync

# Result:
start.time, end.time, compression, message.size, batch.size, total.data.sent.in.MB, MB.sec, total.data.sent.in.nMsg, nMsg.sec
2016-10-06 14:51:07:169, 2016-10-06 14:55:43:092, 0, 100, 8196, 4768.37, 17.2815, 50000000, 181209.9752
```

### 1.3 Single-thread, async 3x replication, batch size: 8196

``` sh
bin/kafka-producer-perf-test.sh --topics test --broker-list hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 --request-num-acks 1 --threads 1 --message-size 100 --messages 50000000 --batch-size 8196

# Result:
start.time, end.time, compression, message.size, batch.size, total.data.sent.in.MB, MB.sec, total.data.sent.in.nMsg, nMsg.sec
2016-10-06 15:02:52:808, 2016-10-06 15:08:09:044, 0, 100, 8196, 4768.37, 15.0785, 50000000, 158109.7661
```

### 1.4 Single-thread, sync 3x replication, no-acks, batch size: 64000

``` sh
bin/kafka-producer-perf-test.sh --topics test --broker-list hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 --request-num-acks -1 --threads 1 --message-size 100 --messages 50000000 --batch-size 64000 --sync

# Results:
start.time, end.time, compression, message.size, batch.size, total.data.sent.in.MB, MB.sec, total.data.sent.in.nMsg, nMsg.sec
Exception in thread "main" kafka.common.InvalidConfigException: Batch size = 64000 can't be larger than queue size = 10000
	at kafka.producer.ProducerConfig$.validateBatchSize(ProducerConfig.scala:39)
	at kafka.producer.ProducerConfig$.validate(ProducerConfig.scala:29)
	at kafka.producer.ProducerConfig.<init>(ProducerConfig.scala:116)
	at kafka.producer.ProducerConfig.<init>(ProducerConfig.scala:56)
	at kafka.producer.OldProducer.<init>(BaseProducer.scala:59)
	at kafka.tools.ProducerPerformance$ProducerThread.<init>(ProducerPerformance.scala:232)
	at kafka.tools.ProducerPerformance$$anonfun$main$1.apply$mcVI$sp(ProducerPerformance.scala:62)
	at scala.collection.immutable.Range.foreach$mVc$sp(Range.scala:141)
	at kafka.tools.ProducerPerformance$.main(ProducerPerformance.scala:61)
	at kafka.tools.ProducerPerformance.main(ProducerPerformance.scala)
```

### 1.5 Three Producers, 3x async replication

On three servers, run:

``` sh
bin/kafka-producer-perf-test.sh --topics test --broker-list hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 --request-num-acks 1 --threads 1 --message-size 100 --messages 50000000 --batch-size 8196

# Results:
```

### 1.6 Throughput Versus Stored Data

``` sh
bin/kafka-producer-perf-test.sh --topics test --broker-list hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 --request-num-acks 1 --threads 1 --message-size 100 --messages 50000000 --batch-size 8196

# Results:
```

### 1.7 Effect of message size

``` sh
for i in 10 100 1000 10000 100000;
  do
    echo ""
    echo $i
    bin/kafka-producer-perf-test.sh --topics test --broker-list hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 --request-num-acks 1 --threads 1 --message-size $i --messages $((1000*1024*1024/$i)) --batch-size 128000
  done;
```


## 2. Consumer

### 2.1 Consumer throughput

``` sh
bin/kafka-consumer-perf-test.sh --zookeeper hdpr01hn01:2181 --messages 50000000 --topic test --threads 1

```

### 2.2 3 Consumers

On three servers, run:

``` sh
bin/kafka-consumer-perf-test.sh --zookeeper hdpr01hn01:2181 --messages 50000000 --topic test --threads 1
```

### 2.4 End-to-end Latency

<https://github.com/apache/kafka/blob/41e676d29587042994a72baa5000a8861a075c8c/tests/kafkatest/services/performance/end_to_end_latency.py>

``` sh
bin/kafka-run-class.sh kafka.tools.TestEndToEndLatency hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 hdpr01hn01:2181 test 5000
```

### 2.5 Producer and consumer

``` sh
bin/kafka-producer-perf-test.sh --topics test --broker-list hdpr01wn01:6667,hdpr01wn02:6667,hdpr01wn03:6667,hdpr01wn04:6667,hdpr01wn05:6667 --request-num-acks 1 --threads 1 --message-size 100 --messages 50000000 --batch-size 8196

bin/kafka-consumer-perf-test.sh --zookeeper hdpr01hn01:2181 --messages 50000000 --topic test --threads 1
```


