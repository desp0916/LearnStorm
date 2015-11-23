package com.pic.ala;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;

import com.pic.ala.spout.RandomLogSpout;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import storm.kafka.bolt.KafkaBolt;
import storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import storm.kafka.bolt.selector.DefaultTopicSelector;

/**
 *
 * storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogKafkaTopology hdp01.localdomain:2181 hdp01.localdomain:6667
 * bin/kafka-console-consumer.sh --zookeeper hdp01.localdomain:2181 --topic aplogtest111 --from-beginning
 *
 */
public class ApLogKafkaTopology {

	private String zkUrl;
	private String brokerUrl;

	public static final String KAFKA_TOPIC = "aplogtest111";

	ApLogKafkaTopology(String zkUrl, String brokerUrl) {
		this.zkUrl = zkUrl;
		this.brokerUrl = brokerUrl;
	}

	public StormTopology buildProducerTopology() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout", new RandomLogSpout(), 2);
		KafkaBolt bolt = new KafkaBolt().withTopicSelector(new DefaultTopicSelector(KAFKA_TOPIC))
				.withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "log"));
		builder.setBolt("forwardToKafka", bolt, 1).shuffleGrouping("spout");
		return builder.createTopology();
	}

	public Config getProducerConfig() {
		Config conf = new Config();
		conf.setMaxSpoutPending(20);
		Properties props = new Properties();
		props.put("metadata.broker.list", brokerUrl);
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("request.required.acks", "1");
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "storm-kafka-producer");
		conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, props);
		return conf;
	}

	/**
	 * <p>
	 * To run this topology ensure you have a kafka broker running.
	 * </p>
	 * Create a topic test with command line, kafka-topics.sh --create
	 * --zookeeper localhost:2181 --replication-factor 1 --partition 1 --topic
	 * test
	 */
	public static void main(String[] args)
			throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {

		String zkUrl = "192.168.20.150:32774";
		String brokerUrl = "192.168.20.150:32775";

		if (args.length > 2 || (args.length == 1 && args[0].matches("^-h|--help$"))) {
			System.out.println("Usage: ApLogKafkaTopology [kafka zookeeper url] [kafka broker url]");
			System.out.println("   E.g ApLogKafkaTopology [" + zkUrl + "]" + " [" + brokerUrl + "]");
			System.exit(1);
		} else if (args.length == 1) {
			zkUrl = args[0];
		} else if (args.length == 2) {
			zkUrl = args[0];
			brokerUrl = args[1];
		}

		ApLogKafkaTopology kafkaToplogy = new ApLogKafkaTopology(zkUrl, brokerUrl);

		System.out.println("zkUrl: " + zkUrl + ", brokerUrl: " + brokerUrl);
//		LocalCluster cluster = new LocalCluster();

		StormSubmitter.submitTopology("ApLogGenerator", kafkaToplogy.getProducerConfig(), kafkaToplogy.buildProducerTopology());

		// StormSubmitter.submitTopology("kafkaBolt",
		// kafkaToplogy.getProducerConfig(),
		// kafkaToplogy.buildProducerTopology());
		// cluster.killTopology("kafkaBolt");
		// cluster.shutdown();
	}

}
