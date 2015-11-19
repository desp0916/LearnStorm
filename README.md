# Learning Apache Storm

```bash
mvn install:install-file -DgroupId=jdk.tools -DartifactId=jdk.tools -Dpackaging=jar -Dversion=1.7 -Dfile=tools.jar -DgeneratePom=true

mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TestTridentTopology
mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TridentWordCount
mvn compile exec:java -Dstorm.topology=com.pic.ala.learn.TestTopology.TridentKafkaWordCount

storm jar target/TestTopology-0.0.1-SNAPSHOT.jar com.pic.ala.learn.TestTopology.TridentKafkaWordCount hdp01.localdomain:2181 hdp02.localdomain:6667
```
