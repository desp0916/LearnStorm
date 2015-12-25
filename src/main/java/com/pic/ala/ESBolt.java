/**
 * ElasticSearch 2.1.1 的作法：
 *
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html
 */
package com.pic.ala;

import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

public class ESBolt extends BaseRichBolt {

	private static final long serialVersionUID = -26161992456930984L;
	private static final Logger LOG = LoggerFactory.getLogger(ESBolt.class);
	private Client client;
	private OutputCollector collector;

	protected String configKey;

	public static final String ES_CLUSTER_NAME = "es.cluster.name";
	public static final String ES_HOST = "es.host";
	public static final String ES_INDEX_NAME = "es.index.nameg";
	public static final String ES_INDEX_TYPE = "es.index.type";

	public ESBolt withConfigKey(String configKey) {
		this.configKey = configKey;
		return this;
	}

	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		Map<String, Object> conf = (Map<String, Object>) stormConf.get(this.configKey);

		String esClusterName = (String) conf.get(ES_CLUSTER_NAME);
		String esHost = (String) conf.get(ES_HOST);
		String esIndexName = (String) conf.get(ES_INDEX_NAME);
		String esIndexType = (String) conf.get(ES_INDEX_TYPE);

		if (conf == null) {
			throw new IllegalArgumentException(
					"ElasticSearch configuration not found using key '" + this.configKey + "'");
		}

		if (esClusterName == null) {
			LOG.warn("No '" + ES_CLUSTER_NAME + "' value found in configuration! Using ElasticSearch defaults.");
		}

		if (esHost == null) {
			LOG.warn("No '" + ES_HOST + "' value found in configuration! Using ElasticSearch defaults.");
		}

		if (esIndexName == null) {
			LOG.warn("No '" + ES_INDEX_NAME + "' value found in configuration! Using ElasticSearch defaults.");
		}

		if (esIndexType == null) {
			LOG.warn("No '" + ES_INDEX_TYPE + "' value found in configuration! Using ElasticSearch defaults.");
		}

//		if ((Boolean) stormConf.get(backtype.storm.Config.TOPOLOGY_DEBUG) == true) {
//			Node node = nodeBuilder().local(true).node();
//			client = node.client();
//		} else {
			try {
//				Settings settings = Settings.settingsBuilder().put("cluster.name", esClusterName).build();
				Settings settings = ImmutableSettings.settingsBuilder()
						.put("cluster.name", esClusterName).build();
				synchronized (ESBolt.class) {
					if (client == null) {
//						client = TransportClient.builder().settings(settings).build().addTransportAddress(
//								new InetSocketTransportAddress(InetAddress.getByName(ES_HOST), 9300));
						client = new TransportClient(settings)
								.addTransportAddress(new InetSocketTransportAddress(esHost, 9300));
					}
				}
			} catch (Exception e) {
				LOG.warn("Unable to initialize ESBolt", e);
			}
//		}
	}

	public void execute(Tuple tuple) {
		String toBeIndexed = (String) tuple.getValueByField(ApLogScheme.FIELD_ES_SOURCE);
		if (toBeIndexed == null) {
			LOG.warn("Received null or incorrect value from tuple");
			return;
		}
		IndexResponse response = client.prepareIndex(ApLogScheme.ES_INDEX, ApLogScheme.LOG_TYPE)
				.setSource(toBeIndexed).execute().actionGet();
		if (response == null)
			LOG.error("Failed to index Tuple: " + tuple.toString());
		else {
			if (response.getId() == null)
				LOG.error("Failed to index Tuple: " + tuple.toString());
			else {
				LOG.debug("Indexing success on Tuple: " + tuple.toString());
//				collector.emit(new Values(entry, response.getId()));
			}
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public void cleanup() {
		client.close();
	}
}
