# Learning Apache Storm

Before using this project, you must install the [shaded jar](https://github.com/desp0916/es-shaded) for ElasticSearch 2.2.1 to your local maven repository.

```bash
mvn install:install-file -DgroupId=jdk.tools -DartifactId=jdk.tools -Dpackaging=jar -Dversion=1.7 -Dfile=tools.jar -DgeneratePom=true

mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TestTridentTopology
mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TridentWordCount
mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TridentKafkaWordCount

storm jar target/TestTopology-0.0.1-SNAPSHOT.jar com.pic.ala.learn.TestTopology.TridentKafkaWordCount hdp01.localdomain:2181 hdp02.localdomain:6667
```

https://azure.microsoft.com/zh-tw/documentation/articles/hdinsight-storm-sensor-data-analysis/

## 1. HOW TO USE:

```bash
# 1. DELETE the old topic & indexes:
/usr/hdp/current/kafka-broker/bin/kafka-topics.sh --zookeeper hdp01.localdomain:2181,hdp02.localdomain:2181,hdp03.localdomain:2181 --delete --topic ap_logs_test_222

curl -XDELETE --user es_admin:password 'localhost:9200/aplog*?pretty'

# 1.1 Delete the topic znode on Zookeeper by zookeeper-client
# 1.2 Delete the topic commit logs
# 1.3 Create the Elasticsearch index template if needed

curl -XPUT -u es_admin:password "http://localhost:9200/_template/aplog_1?pretty=true" -d  '
{
  "template": "aplog_*",
  "settings": {
    "number_of_replicas": 3,
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
mvn clean package

# 5. SUBMIT Storm topology: ApLogAnalyzer
storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogAnalyzer

# 6. SUBMIT Storm topology: ApLogGenerator
storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogGenerator

# 7. MONITOR the logs with Kibana
```

## 2. Kafka Maintainance

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
