/**
 * storm jar target/LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogAnalysisTopology
 *
 *
 * storm jar target\LearnStorm-0.0.1-SNAPSHOT.jar com.pic.ala.ApLogAnalysisTopology -c nimbus.host=192.168.20.150 -c nimbus.thrift.port=49627
 */
package com.pic.ala;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
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

public class ApLogAnalysisTopology extends ApLogBaseTopology {

	private static final Logger LOG = LoggerFactory.getLogger(ApLogAnalysisTopology.class);
	private static final String KAFKA_SPOUT_ID = "kafkaSpout";
	private static final String HBASE_BOLT1_ID = "hbaseDetailBolt";
	private static final String HBASE_BOLT2_ID = "hbaseAggBolt";
	private static final String ES_BOLT_ID = "esBolt";

	public ApLogAnalysisTopology(String configFileLocation) throws Exception {
		super(configFileLocation);
	}

	private SpoutConfig constructKafkaSpoutConf() {
		BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
		String topic = topologyConfig.getProperty("kafka.topic");
		String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
		// String consumerGroupId = "ApLogAnalysisSpout";
		String consumerGroupId = UUID.randomUUID().toString();
		// String clientId = "ApLogAnalysisClient";
		// KafkaConfig kafkaConfig = new KafkaConfig(hosts, topic, clientId);
		SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, consumerGroupId);
		spoutConfig.startOffsetTime = System.currentTimeMillis();
		// spoutConfig.startOffsetTime = kafka.api.OffsetRequest.EarliestTime()
		spoutConfig.scheme = new SchemeAsMultiScheme(new ApLogScheme());
		return spoutConfig;
	}

	public void configureKafkaSpout(TopologyBuilder builder, Config conf) {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		int spoutCount = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
		builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, spoutCount);
	}

	public void configureESBolt(TopologyBuilder builder, Config conf) {
		Map<String, Object> esConf = new HashMap<String, Object>();
		esConf.put(ESBolt.ES_CLUSTER_NAME, topologyConfig.getProperty("es.cluster.name"));
		esConf.put(ESBolt.ES_HOST, topologyConfig.getProperty("es.host"));
		esConf.put(ESBolt.ES_INDEX_NAME, topologyConfig.getProperty("es.index.name"));
		esConf.put(ESBolt.ES_INDEX_TYPE, topologyConfig.getProperty("es.index.type"));
		conf.put("es.conf", esConf);
		ESBolt esBolt = new ESBolt().withConfigKey("es.conf");
		builder.setBolt(ES_BOLT_ID, esBolt, 1).shuffleGrouping(KAFKA_SPOUT_ID);
	}

	public void configureHBaseBolts(TopologyBuilder builder, Config conf) {

		Map<String, Object> hbConf = new HashMap<String, Object>();
		hbConf.put("hbase.rootdir", "hdfs://hdpha/apps/hbase/data");
		conf.put("hbase.conf", hbConf);

		SimpleHBaseMapper mapper = new SimpleHBaseMapper().withRowKeyField(ApLogScheme.FIELD_LOG_ID)
				.withColumnFields(new Fields(ApLogScheme.FIELD_HOSTIP, ApLogScheme.FIELD_LOG_TIME,
						ApLogScheme.FIELD_LOG_LEVEL, ApLogScheme.FIELD_CLASS_METHOD, ApLogScheme.FIELD_KEYWORD1,
						ApLogScheme.FIELD_KEYWORD2, ApLogScheme.FIELD_KEYWORD3, ApLogScheme.FIELD_MESSAGE))
				// .withCounterFields(new Fields("count"))
				.withColumnFamily("cf");
		HBaseBolt hbase1 = new HBaseBolt(ApLogScheme.SYSTEM_ID, mapper).withConfigKey("hbase.conf");
		builder.setBolt(HBASE_BOLT1_ID, hbase1, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(ApLogScheme.FIELD_LOG_ID));

		HBaseBolt hbase2 = new CustomHBaseBolt(ApLogScheme.SYSTEM_ID, mapper).withConfigKey("hbase.conf");
		builder.setBolt(HBASE_BOLT2_ID, hbase2, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(ApLogScheme.FIELD_LOG_ID));
	}

	private void buildAndSubmit() throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		TopologyBuilder builder = new TopologyBuilder();
		Config conf = new Config();
		conf.setDebug(true);
		configureKafkaSpout(builder, conf);
//		configureHBaseBolts(builder, conf);
		configureESBolt(builder, conf);
		// conf.put("topology.auto-credentials",
		// "org.apache.storm.hbase.security.AutoHBase");

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("ApLogAnalyzer", conf, builder.createTopology());
	}

	public static void main(String args[]) throws Exception {
		String configFileLocation = "ApLogAnalyzer.properties";
		ApLogAnalysisTopology apLogAnalysisTopology = new ApLogAnalysisTopology(configFileLocation);
		apLogAnalysisTopology.buildAndSubmit();
	}

}