# Learning Apache Storm

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

curl -XDELETE 'localhost:9200/aplog_aes3g*?pretty'
curl -XDELETE 'localhost:9200/aplog_pos*?pretty'
curl -XDELETE 'localhost:9200/aplog_upcc*?pretty'
curl -XDELETE 'localhost:9200/aplog_wds*?pretty'

# 1.1 Delete the topic znode on Zookeeper by zookeeper-client
# 1.2 Delete the topic commit logs

# 2. RECREATE the topic & indexes:
/usr/hdp/current/kafka-broker/bin/kafka-topics.sh --create --zookeeper hdp01.localdomain:2181,hdp02.localdomain:2181,hdp03.localdomain:2181 --replication-factor 1 --partition 6 --topic ap_logs_test_222

curl -XPUT 'localhost:9200/aplog_aes3g?pretty'
curl -XPUT 'localhost:9200/aplog_pos?pretty'
curl -XPUT 'localhost:9200/aplog_upcc?pretty'
curl -XPUT 'localhost:9200/aplog_wds?pretty'

# 3. START monitoring the topic:
/usr/hdp/current/kafka-broker/bin/kafka-console-consumer.sh --zookeeper hdp01.localdomain:2181 --topic ap_logs_test_222 --from-beginning

# 4. COMPILE & PACKAGE Storm topologies:
cd /root/workspace/LearnStorm/
mvn clean package

# 5. SUBMIT Storm topology: ApLogAnalyzer
storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogAnalyzer

# 6. SUBMIT Storm topology: ApLogGenerator
storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogGenerator

# 7. MODIFY Elasticsearch Mappings:

curl -XPUT 'http://localhost:9200/aplog_*/_mapping/*' -d  '{
      "properties" : {
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
}'

# 8. MONITOR the logs with Kibana
```


## 2. References:

 * [Unofficial Storm and Kafka Best Practices Guide](https://community.hortonworks.com/articles/550/unofficial-storm-and-kafka-best-practices-guide.html)
 * [KafkaSpout 浅析](http://www.cnblogs.com/cruze/p/4241181.html)
