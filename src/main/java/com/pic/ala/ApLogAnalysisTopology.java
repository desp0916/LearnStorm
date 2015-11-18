package com.pic.ala;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import storm.kafka.BrokerHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.ZkHosts;

public class ApLogAnalysisTopology extends BaseApLogAnalysisTopology {

	private static final Logger LOG = Logger.getLogger(ApLogAnalysisTopology.class);
	private static final String KAFKA_SPOUT_ID = "kafkaSpout";
	private static final String HBASE_BOLT_ID = "hbaseBolt";

	private Properties topologyConfig;

	public ApLogAnalysisTopology(String configFileLocation) throws Exception {
		super(configFileLocation);
	}

	private SpoutConfig constructKafkaSpoutConf() {
		BrokerHosts hosts = new ZkHosts(topologyConfig.getProperty("kafka.zookeeper.host.port"));
		String topic = topologyConfig.getProperty("kafka.topic");
		String zkRoot = topologyConfig.getProperty("kafka.zkRoot");
		String consumerGroupId = "StormSpout";
		SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, consumerGroupId);
		spoutConfig.scheme = new SchemeAsMultiScheme(new ApLogScheme());

		return spoutConfig;
	}

	public void configureKafkaSpout(TopologyBuilder builder) {
		KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
		int spoutCount = Integer.valueOf(topologyConfig.getProperty("spout.thread.count"));
		builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout, spoutCount);
	}

	public void configureHBaseBolt(TopologyBuilder builder) {
		SimpleHBaseMapper mapper = new SimpleHBaseMapper()
		        .withRowKeyField(ApLogScheme.FIELD_LOG_ID)
				.withColumnFields(new Fields(ApLogScheme.FIELD_HOSTNAME,
									ApLogScheme.FIELD_EXEC_TIME,
									ApLogScheme.FIELD_ERROR_LEVEL,
									ApLogScheme.FIELD_EXEC_METHOD,
									ApLogScheme.FIELD_KEYWORD1,
									ApLogScheme.FIELD_KEYWORD2,
									ApLogScheme.FIELD_KEYWORD3,
									ApLogScheme.FIELD_MESSAGE))
//		        .withCounterFields(new Fields("count"))
		        .withColumnFamily("cf");
		HBaseBolt hbase = new HBaseBolt(ApLogScheme.SYSTEM_ID, mapper);
		builder.setBolt(HBASE_BOLT_ID, hbase, 1).fieldsGrouping(KAFKA_SPOUT_ID, new Fields(ApLogScheme.FIELD_LOG_ID));
	}

	private void buildAndSubmit() throws Exception {
		TopologyBuilder builder = new TopologyBuilder();
		configureKafkaSpout(builder);
		configureHBaseBolt(builder);
		Config conf = new Config();
		conf.setDebug(true);
		StormSubmitter.submitTopology("ap-log-analyzer", conf, builder.createTopology());
	}

	public static void main(String args[]) throws Exception {
		String configFileLocation = "ap_log_analysis_topology.properties";
		ApLogAnalysisTopology apLogAnalysisTopology = new ApLogAnalysisTopology(configFileLocation);
		apLogAnalysisTopology.buildAndSubmit();
	}

}