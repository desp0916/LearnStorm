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
 * 3. Understanding the Parallelism of a Storm Topology
 *
 *   http://www.michael-noll.com/blog/2012/10/16/understanding-the-parallelism-of-a-storm-topology/
 *
 * 4. KafkaSpout 浅析
 *
 *   http://www.cnblogs.com/cruze/p/4241181.html
 *
 * 5. Unofficial Storm and Kafka Best Practices Guide
 *
 *   https://community.hortonworks.com/articles/550/unofficial-storm-and-kafka-best-practices-guide.html
 */

package com.pic.ala;

import java.util.HashMap;

import com.pic.ala.bolt.ESIndexerBolt;
import com.pic.ala.scheme.ApLogScheme;

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

public class ApLogAnalyzer extends LogBaseTopology {
	
	private static boolean DEBUG = false;

//	private static final Logger LOG = Logger.getLogger(ApLogAnalyzer.class);

	private static final String KAFKA_SPOUT_ID = "kafkaSpout";
	private static final String ESINDEXER_BOLT_ID = "ESIndexerBolt";
//	private static final String HBASE_DETAIL_BOLT_ID = "hbaseDetailBolt";
//	private static final String HBASE_AGG_BOLT_ID = "hbaseAggBolt";
	private static final String CONSUMER_GROUP_ID = "aplog-analyzer";
	private ApLogScheme apLogScheme;

	public ApLogAnalyzer(String configFileLocation) throws Exception {
		super(configFileLocation);
		apLogScheme = new ApLogScheme();
	}

	private SpoutConfig constructKafkaSpoutConf() {
		final BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
		final String topic = topologyConfig.getProperty("kafka.topic");
		final String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
//		String consumerGroupId = UUID.randomUUID().toString();
		final SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, CONSUMER_GROUP_ID);
		spoutConfig.startOffsetTime = System.currentTimeMillis();
		spoutConfig.scheme = new SchemeAsMultiScheme(apLogScheme);
		spoutConfig.retryInitialDelayMs = 10000;
		spoutConfig.retryDelayMultiplier = 1.0;
		return spoutConfig;
	}

	private void configureKafkaSpout(TopologyBuilder builder, Config config) {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		final int spoutThreads = Integer.valueOf(topologyConfig.getProperty("spout.KafkaSpout.threads"));

		builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, spoutThreads).setDebug(DEBUG);
	}

	private void configureESBolts(TopologyBuilder builder, Config config) {
		HashMap<String, Object> esConfig = new HashMap<String, Object>();
		esConfig.put(ESIndexerBolt.ES_CLUSTER_NAME, topologyConfig.getProperty(ESIndexerBolt.ES_CLUSTER_NAME));
		esConfig.put(ESIndexerBolt.ES_NODES, topologyConfig.getProperty(ESIndexerBolt.ES_NODES));
		esConfig.put(ESIndexerBolt.ES_SHIELD_ENABLED, topologyConfig.getProperty(ESIndexerBolt.ES_SHIELD_ENABLED));
		esConfig.put(ESIndexerBolt.ES_SHIELD_USER, topologyConfig.getProperty(ESIndexerBolt.ES_SHIELD_USER));
		esConfig.put(ESIndexerBolt.ES_SHIELD_PASS, topologyConfig.getProperty(ESIndexerBolt.ES_SHIELD_PASS));
		esConfig.put(ESIndexerBolt.ES_ASYNC_ENABLED, topologyConfig.getProperty(ESIndexerBolt.ES_ASYNC_ENABLED));
		config.put("es.conf", esConfig);
		ESIndexerBolt esBolt = new ESIndexerBolt().withConfigKey("es.conf");
		final int boltThreads = Integer.valueOf(topologyConfig.getProperty("bolt.ESIndexerBolt.threads"));

		builder.setBolt(ESINDEXER_BOLT_ID, esBolt, boltThreads).shuffleGrouping(KAFKA_SPOUT_ID).setDebug(DEBUG);
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
		final int numWorkers = Integer.valueOf(topologyConfig.getProperty("num.workers"));
		Config config = new Config();
		config.setDebug(DEBUG);
		config.setNumWorkers(numWorkers);
		config.setMaxSpoutPending(20);

		TopologyBuilder builder = new TopologyBuilder();
		configureKafkaSpout(builder, config);
		configureESBolts(builder, config);
//		configureHBaseBolts(builder, config);

//		conf.put(Config.NIMBUS_HOST, "hdp01.localdomain");
//		System.setProperty("storm.jar", "/root/workspace//LearnStorm/target/LearnStorm-0.0.1-SNAPSHOT.jar");
//		System.setProperty("hadoop.home.dir", "/tmp");
//		LocalCluster cluster = new LocalCluster();
		StormSubmitter.submitTopology("ApLogAnalyzerV1", config, builder.createTopology());
	}

	public static void main(String args[]) throws Exception {
		final String configFileLocation = "ApLogAnalyzer.properties";
		ApLogAnalyzer topology = new ApLogAnalyzer(configFileLocation);
		topology.buildAndSubmit();
	}

}