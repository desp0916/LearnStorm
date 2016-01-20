/**
 * Ref: https://github.com/apache/storm/tree/master/external/storm-kafka
 *
 * 1. Create a table with HBase shell:
 *
 *     create 'aes3g', 'cf'
 *
 * 2. Submit this topology to consume the topic on Kafka and ingest into Hbase:
 *
 *     storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogAnalyzer
 *
 */

package com.pic.ala;

import java.util.HashMap;

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

public class ApLogAnalyzer extends ApLogBaseTopology {

//	private static final Logger LOG = Logger.getLogger(ApLogAnalyzer.class);
	private static final String KAFKA_SPOUT_ID = "kafkaSpout";
	private static final String ES_BOLT_ID = "ESBolt";
	private static final String HBASE_DETAIL_BOLT_ID = "hbaseDetailBolt";
	private static final String HBASE_AGG_BOLT_ID = "hbaseAggBolt";
	private static final String CONSUMER_GROUP_ID = "ApLogAnalyzerSpout";
	private ApLogScheme apLogScheme;

	public ApLogAnalyzer(String configFileLocation) throws Exception {
		super(configFileLocation);
		apLogScheme = new ApLogScheme();
	}

	private SpoutConfig constructKafkaSpoutConf() {
		BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
		String topic = topologyConfig.getProperty("kafka.topic");
		String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
//		String consumerGroupId = UUID.randomUUID().toString();
		SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, CONSUMER_GROUP_ID);
		spoutConfig.startOffsetTime = System.currentTimeMillis();
		spoutConfig.scheme = new SchemeAsMultiScheme(apLogScheme);
		return spoutConfig;
	}

	private void configureKafkaSpout(TopologyBuilder builder, Config config) {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		int spoutThreads = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
		builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, spoutThreads);
	}

	private void configureESBolts(TopologyBuilder builder, Config config) {
		HashMap<String, Object> esConfig = new HashMap<String, Object>();
		esConfig.put(ESBolt.ES_CLUSTER_NAME, topologyConfig.getProperty(ESBolt.ES_CLUSTER_NAME));
		esConfig.put(ESBolt.ES_HOST, topologyConfig.getProperty(ESBolt.ES_HOST));
		esConfig.put(ESBolt.ES_INDEX_NAME, topologyConfig.getProperty(ESBolt.ES_INDEX_NAME));
		esConfig.put(ESBolt.ES_INDEX_TYPE, topologyConfig.getProperty(ESBolt.ES_INDEX_TYPE));
		config.put("es.conf", esConfig);

		ESBolt esBolt = new ESBolt().withConfigKey("es.conf");
		builder.setBolt(ES_BOLT_ID, esBolt, 3).shuffleGrouping(KAFKA_SPOUT_ID);

//		EsConfig esConfig = new EsConfig("elasticsearch", new String[]{"hdp01.localdomain:9300"});
//		EsTupleMapper tupleMapper = new CustomEsTupleMapper();
//		EsIndexBolt indexBolt = new EsIndexBolt(esConfig, tupleMapper);
//		builder.setBolt(ES_BOLT_ID, indexBolt, 1).shuffleGrouping(KAFKA_SPOUT_ID);
	}

//	private void configureHBaseBolts(TopologyBuilder builder, Config config) {
//		Map<String, Object> hbaseConfig = new HashMap<String, Object>();
//		hbaseConfig.put("hbase.rootdir", "hdfs://hdpha/apps/hbase/data");
//		config.put("hbase.conf", hbaseConfig);
//
//		// the bolt to write raw AP logs
//		SimpleHBaseMapper rawMapper = new SimpleHBaseMapper()
//				.withRowKeyField(ApLogScheme.FIELD_ROWKEY)
//				.withColumnFields(new Fields(
//						ApLogScheme.FIELD_HOSTIP,
//						ApLogScheme.FIELD_LOG_TIME,
//						ApLogScheme.FIELD_LOG_LEVEL,
//						ApLogScheme.FIELD_CLASS_METHOD,
//						ApLogScheme.FIELD_KEYWORD1,
//						ApLogScheme.FIELD_KEYWORD2,
//						ApLogScheme.FIELD_KEYWORD3,
//						ApLogScheme.FIELD_MESSAGE))
////				.withCounterFields(new Fields("count"))
//				.withColumnFamily("cf");
//		HBaseBolt hbase = new HBaseBolt(APLogScheme.SYSTEM_ID, rawMapper).withConfigKey("hbase.conf");
//		builder.setBolt(HBASE_DETAIL_BOLT_ID, hbase, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(APLogScheme.FIELD_LOG_ID));
//
//		// the bolt to write aggregations
//		SimpleHBaseMapper aggMapper = new SimpleHBaseMapper();
//		HBaseBolt hbase2 = new CustomHBaseBolt(APLogScheme.AGG_TABLE, aggMapper).withConfigKey("hbase.conf");
//		builder.setBolt(HBASE_AGG_BOLT_ID, hbase2, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(APLogScheme.FIELD_AGG_ID));
//	}

	private void buildAndSubmit() throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		Config config = new Config();
		config.setDebug(true);
		config.setNumWorkers(3);
		config.setMaxSpoutPending(20);

		TopologyBuilder builder = new TopologyBuilder();
		configureKafkaSpout(builder, config);
//		configureHBaseBolts(builder, config);
		configureESBolts(builder, config);

//		conf.put(Config.NIMBUS_HOST, "hdp01.localdomain");
//		System.setProperty("storm.jar", "/root/workspace//LearnStorm/target/LearnStorm-0.0.1-SNAPSHOT.jar");
//		System.setProperty("hadoop.home.dir", "/tmp");
//		LocalCluster cluster = new LocalCluster();
		StormSubmitter.submitTopology("ApLogAnalyzer", config, builder.createTopology());
	}

	public static void main(String args[]) throws Exception {
		String configFileLocation = "ApLogAnalyzer.properties";
		ApLogAnalyzer topology = new ApLogAnalyzer(configFileLocation);
		topology.buildAndSubmit();
	}

}