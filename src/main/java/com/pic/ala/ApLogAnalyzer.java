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
import java.util.Map;

import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

public class ApLogAnalyzer extends ApLogBaseTopology {

//	private static final Logger LOG = Logger.getLogger(ApLogAnalyzer.class);
	private static final String KAFKA_SPOUT_ID = "kafkaSpout";
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

	private void configureKafkaSpout(TopologyBuilder builder) {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		int spoutThreads = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
		builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, spoutThreads);
	}

	private void configureHBaseBolts(TopologyBuilder builder) {
		// the bolt to write raw AP logs
		SimpleHBaseMapper rawMapper = new SimpleHBaseMapper()
				.withRowKeyField(ApLogScheme.FIELD_LOG_ID)
				.withColumnFields(new Fields(
						ApLogScheme.FIELD_HOSTNAME,
						ApLogScheme.FIELD_EXEC_TIME,
						ApLogScheme.FIELD_ERROR_LEVEL,
						ApLogScheme.FIELD_EXEC_METHOD,
						ApLogScheme.FIELD_KEYWORD1,
						ApLogScheme.FIELD_KEYWORD2,
						ApLogScheme.FIELD_KEYWORD3,
						ApLogScheme.FIELD_MESSAGE))
//				.withCounterFields(new Fields("count"))
				.withColumnFamily("cf");
		HBaseBolt hbase = new HBaseBolt(ApLogScheme.SYSTEM_ID, rawMapper).withConfigKey("hbase.conf");
		builder.setBolt(HBASE_DETAIL_BOLT_ID, hbase, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(ApLogScheme.FIELD_LOG_ID));

		// the bolt to write aggregations
		SimpleHBaseMapper aggMapper = new SimpleHBaseMapper();
		HBaseBolt hbase2 = new CustomHBaseBolt(ApLogScheme.AGG_TABLE, aggMapper).withConfigKey("hbase.conf");
		builder.setBolt(HBASE_AGG_BOLT_ID, hbase2, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(ApLogScheme.FIELD_AGG_ID));
	}

	private void buildAndSubmit() throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		TopologyBuilder builder = new TopologyBuilder();
		configureKafkaSpout(builder);
		configureHBaseBolts(builder);
		Config conf = new Config();
		Map<String, Object> hbConf = new HashMap<String, Object>();

		hbConf.put("hbase.rootdir", "hdfs://hdpha/apps/hbase/data");
		conf.put("hbase.conf", hbConf);
		conf.setDebug(true);
//		LocalCluster cluster = new LocalCluster();
//		conf.put(Config.NIMBUS_HOST, "hdp01.localdomain");
//		System.setProperty("storm.jar", "/root/workspace//LearnStorm/target/LearnStorm-0.0.1-SNAPSHOT.jar");
		System.setProperty("hadoop.home.dir", "/tmp");
		StormSubmitter.submitTopology("ApLogAnalyzer", conf, builder.createTopology());
	}

	public static void main(String args[]) throws Exception {
		String configFileLocation = "ApLogAnalyzer.properties";
		ApLogAnalyzer topology = new ApLogAnalyzer(configFileLocation);
		topology.buildAndSubmit();
	}

}