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
	private static final String HBASE_BOLT_ID = "hbaseBolt";
	private static final String CONSUMER_GROUP_ID = "ApLogAnalyzerSpout";

	public ApLogAnalyzer(String configFileLocation) throws Exception {
		super(configFileLocation);
	}

	private SpoutConfig constructKafkaSpoutConf() {
		BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
		String topic = topologyConfig.getProperty("kafka.topic");
		String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
//		String consumerGroupId = UUID.randomUUID().toString();
		SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, CONSUMER_GROUP_ID);
		spoutConfig.startOffsetTime = System.currentTimeMillis();
		spoutConfig.scheme = new SchemeAsMultiScheme(new ApLogScheme());
		return spoutConfig;
	}

	public void configureKafkaSpout(TopologyBuilder builder) {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		int spoutThreads = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
		builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, spoutThreads);
	}

	public void configureHBaseBolt(TopologyBuilder builder) {
		SimpleHBaseMapper mapper = new SimpleHBaseMapper()
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
		HBaseBolt hbase = new HBaseBolt(ApLogScheme.SYSTEM_ID, mapper).withConfigKey("hbase.conf");
		builder.setBolt(HBASE_BOLT_ID, hbase, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(ApLogScheme.FIELD_LOG_ID));
	}

	private void buildAndSubmit() throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		TopologyBuilder builder = new TopologyBuilder();
		configureKafkaSpout(builder);
		configureHBaseBolt(builder);
		Config conf = new Config();
		Map<String, Object> hbConf = new HashMap<String, Object>();

		hbConf.put("hbase.rootdir", "hdfs://hdpha/apps/hbase/data");
		conf.put("hbase.conf", hbConf);
		conf.setDebug(true);
//		LocalCluster cluster = new LocalCluster();
		StormSubmitter.submitTopology("ApLogAnalyzer", conf, builder.createTopology());
	}

	public static void main(String args[]) throws Exception {
		String configFileLocation = "ApLogAnalyzer.properties";
		ApLogAnalyzer topology = new ApLogAnalyzer(configFileLocation);
		topology.buildAndSubmit();
	}

}