# Learning Apache Storm

There are two Storm topologies for [ELK stack](https://www.elastic.co/products).

  - [LogAnalyzer](src/main/java/com/pic/ala/LogAnalyzer.java): You can use it to ingest log stream from general Logstash json output through Kafka to Elasticsearch.
  - [ApLogAnalyzer](src/main/java/com/pic/ala/ApLogAnalyzer.java): It is similar to the above one. Use it to ingest log stream from customized Logstash json output through Kafka to Elasticsearch.
  
The overall data flow works as the following diagram:

```
log stream ==> Logstash ==> Kafka ==> Storm Topology ==> Elasticsearch
```

Before using this project, you must install the [shaded jar](https://github.com/desp0916/es-shaded) for ElasticSearch 2.3.2 to your local Maven repository.

JDK 8 or above is required. Also, you have to install `tools.jar` first, which is normally locate at `$JAVA_HOME/lib/tools.jar`.

```bash
cd $JAVA_HOME
mvn install:install-file -DgroupId=jdk.tools -DartifactId=jdk.tools -Dpackaging=jar -Dversion=1.8 -Dfile=tools.jar -DgeneratePom=true
```

## 1. HOW TO USE:

```bash
# 1. DELETE the old topic & indexes:
/usr/hdp/current/kafka-broker/bin/kafka-topics.sh --zookeeper hdp01.localdomain:2181,hdp02.localdomain:2181,hdp03.localdomain:2181 --delete --topic ap_logs_test_222

curl -XDELETE --user es_admin:password 'localhost:9200/aplog*?pretty'

# 1.1 Delete the topic znode on Zookeeper by zookeeper-client
# 1.2 Delete the topic commit logs
# 1.3 Create the Elasticsearch index template if needed

curl -XPUT -u es_admin:password "http://localhost:9200/_template/aplog_*?pretty=true" -d  '
{
  "template": "aplog_*",
  "settings": {
    "number_of_replicas": 1,
    "number_of_shards": 5
  },
  "mappings": {
    "*": {
      "_source": {
        "enabled": true
      },
      "properties": {
        "apID" : {
          "type" : "string",
          "index": "not_analyzed" 
        },
        "functID" : {
          "type" : "string",
          "index": "not_analyzed" 
        },
        "logType" : {
          "type" : "string",
          "index": "not_analyzed" 
        },
        "msgLevel" : {
          "type" : "string",
          "index": "not_analyzed"
        },
        "sysID" : {
          "type" : "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'

# 2. RECREATE the topic:
/usr/hdp/current/kafka-broker/bin/kafka-topics.sh --create --zookeeper hdp01.localdomain:2181,hdp02.localdomain:2181,hdp03.localdomain:2181 --replication-factor 2 --partition 10 --topic ap_logs_test_222


# 3. START monitoring the topic:
/usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --zookeeper hdp01.localdomain:2181 --topic ap_logs_test_222 --from-beginning

# 4. COMPILE & PACKAGE Storm topologies:
cd /root/workspace/LearnStorm/
mvn clean package -DskipTests

# 5. SUBMIT Storm topology: ApLogAnalyzer
storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogAnalyzer

# 6. SUBMIT Storm topology: ApLogGenerator
storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogGenerator

# 7. MONITOR the logs with Kibana
```

## 2. Commands for Kafka Maintainance

```bash
/usr/hdp/current/kafka-broker/bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --zookeeper hdp01.localdomain:2181 --group aplog-analyzer
/usr/hdp/current/kafka-broker/bin/kafka-topics.sh --zookeeper hdp01.localdomain:2181 --topic ap_logs_test_222 --describe
```

## 3. References:

 * [Unofficial Storm and Kafka Best Practices Guide](https://community.hortonworks.com/articles/550/unofficial-storm-and-kafka-best-practices-guide.html)
 * [KafkaSpout 浅析](http://www.cnblogs.com/cruze/p/4241181.html)
 * [STORM : How do I fix the google.guava dependency while running mvn clean install -DskipTests=true ?](https://community.hortonworks.com/questions/14998/storm-how-do-i-fix-the-googleguava-dependency-whil.html)
 * [What is the maven-shade-plugin used for, and why would you want to relocate java packages?](http://stackoverflow.com/questions/13620281/what-is-the-maven-shade-plugin-used-for-and-why-would-you-want-to-relocate-java)
 * [Apache Maven Shade Plugin / shade:shade](https://maven.apache.org/plugins/maven-shade-plugin/shade-mojo.html)
 * [在 HDInsight (Hadoop) 中使用 Apache Storm、事件中樞和 HBase 分析感應器資料](https://azure.microsoft.com/zh-tw/documentation/articles/hdinsight-storm-sensor-data-analysis/)


## 4. Misc

PLEASE IGNORE THIS SECTION.

```bash
mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TestTridentTopology
mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TridentWordCount
mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TridentKafkaWordCount

storm jar target/TestTopology-0.0.1-SNAPSHOT.jar com.pic.ala.learn.TestTopology.TridentKafkaWordCount hdp01.localdomain:2181 hdp02.localdomain:6667
```

