package com.pic.ala;

import java.util.HashMap;

import com.pic.ala.bolt.ESIndexBolt;
import com.pic.ala.scheme.LogScheme;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

public class LogAnalyzer extends LogBaseTopology {
	
	private static boolean DEBUG = false;

	private static final String KAFKA_SPOUT_ID = "kafkaSpout";
	private static final String ESINDEX_BOLT_ID = "ESIndexBolt";
	private static final String CONSUMER_GROUP_ID = "log-analyzer";
	private LogScheme logScheme;

	public LogAnalyzer(String configFileLocation) throws Exception {
		super(configFileLocation);
		logScheme = new LogScheme();
	}

	private SpoutConfig constructKafkaSpoutConf() {
		final BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
		final String topic = topologyConfig.getProperty("kafka.topic");
		final String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
//		String consumerGroupId = UUID.randomUUID().toString();
		final SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, CONSUMER_GROUP_ID);
		spoutConfig.startOffsetTime = System.currentTimeMillis();
		spoutConfig.scheme = new SchemeAsMultiScheme(logScheme);
		return spoutConfig;
	}

	private void configureKafkaSpout(TopologyBuilder builder, Config config) {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		final int spoutThreads = Integer.valueOf(topologyConfig.getProperty("spout.KafkaSpout.threads"));

		builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, spoutThreads).setDebug(DEBUG);
	}

	private void configureESBolts(TopologyBuilder builder, Config config) {
		HashMap<String, Object> esConfig = new HashMap<String, Object>();
		esConfig.put(ESIndexBolt.ES_CLUSTER_NAME, topologyConfig.getProperty(ESIndexBolt.ES_CLUSTER_NAME));
		esConfig.put(ESIndexBolt.ES_NODES, topologyConfig.getProperty(ESIndexBolt.ES_NODES));
		esConfig.put(ESIndexBolt.ES_SHIELD_ENABLED, topologyConfig.getProperty(ESIndexBolt.ES_SHIELD_ENABLED));
		esConfig.put(ESIndexBolt.ES_SHIELD_USER, topologyConfig.getProperty(ESIndexBolt.ES_SHIELD_USER));
		esConfig.put(ESIndexBolt.ES_SHIELD_PASS, topologyConfig.getProperty(ESIndexBolt.ES_SHIELD_PASS));
		esConfig.put(ESIndexBolt.ES_INDEX_NAME, topologyConfig.getProperty(ESIndexBolt.ES_INDEX_NAME));
		esConfig.put(ESIndexBolt.ES_INDEX_TYPE, topologyConfig.getProperty(ESIndexBolt.ES_INDEX_TYPE));
		esConfig.put(ESIndexBolt.ES_ASYNC_ENABLED, topologyConfig.getProperty(ESIndexBolt.ES_ASYNC_ENABLED));
		config.put("es.conf", esConfig);
		ESIndexBolt esBolt = new ESIndexBolt().withConfigKey("es.conf");
		final int boltThreads = Integer.valueOf(topologyConfig.getProperty("bolt.ESIndexBolt.threads"));

		builder.setBolt(ESINDEX_BOLT_ID, esBolt, boltThreads).shuffleGrouping(KAFKA_SPOUT_ID).setDebug(DEBUG);
	}

	private void buildAndSubmit() throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		final int numWorkers = Integer.valueOf(topologyConfig.getProperty("num.workers"));
		Config config = new Config();
		config.setDebug(DEBUG);
		config.setNumWorkers(numWorkers);
		config.setMaxSpoutPending(20);

		TopologyBuilder builder = new TopologyBuilder();
		configureKafkaSpout(builder, config);
		configureESBolts(builder, config);

//		LocalCluster cluster = new LocalCluster();
		StormSubmitter.submitTopology("LogAnalyzerV1", config, builder.createTopology());
	}

	public static void main(String args[]) throws Exception {
		final String configFileLocation = "LogAnalyzer.properties";
		LogAnalyzer topology = new LogAnalyzer(configFileLocation);
		topology.buildAndSubmit();
	}

}