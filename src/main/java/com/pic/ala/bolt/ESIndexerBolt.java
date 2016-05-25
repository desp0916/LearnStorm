/**
 * ElasticSearch 最新版的作法：
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html
 *
 * Adding mapping to a type from Java - how do I do it?
 * http://stackoverflow.com/questions/22071198/adding-mapping-to-a-type-from-java-how-do-i-do-it
 *
 * At first, you should create the index just like this:
 *  curl -XPUT 'localhost:9200/aplog_aes3g?pretty'
 *  curl -XPUT 'localhost:9200/aplog_pos?pretty'
 *  curl -XPUT 'localhost:9200/aplog_upcc?pretty'
 *  curl -XPUT 'localhost:9200/aplog_wds?pretty'
 *
 * ElasticSearch - Index document:
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/1.7/index-doc.html
 */

package com.pic.ala.bolt;

import static com.pic.ala.util.LogUtil.isDateValid;
import static com.pic.ala.util.LogUtil.isNullOrEmpty;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.shield.ShieldPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pic.ala.scheme.ApLogScheme;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ESIndexerBolt extends BaseRichBolt {

    private static final Logger LOG = LoggerFactory.getLogger(ESIndexerBolt.class);
	private static final String ES_INDEX_PREFIX = "aplog_";
	private static Client client;
	private static TransportClient transportClient;
	private OutputCollector collector;

	protected String configKey;

	public static final String ES_CLUSTER_NAME = "es.cluster.name";
	public static final String ES_NODES = "es.nodes";
	public static final String ES_SHIELD_ENABLED = "es.shield.enabled";
	public static final String ES_SHIELD_USER = "es.shield.user";
	public static final String ES_SHIELD_PASS = "es.shield.pass";
	public static final int MIN_CONNECTED_NODES = 5;
//	public static final String ES_INDEX_NAME = "es.index.name";
//	public static final String ES_INDEX_TYPE = "es.index.type";
	public static final String ES_ASYNC_ENABLED = "es.async.enabled";

	// DO NOT MODIFY HERE.
	// Instead modify the setting "es.async.enabled" in "LogAnalyzer.properties" file.
	private static boolean esAsyncEnabled = true;

	public ESIndexerBolt withConfigKey(final String configKey) {
		this.configKey = configKey;
		return this;
	}

	/**
	 * @TODO add mapping, see:
	 * http://stackoverflow.com/questions/22071198/adding-mapping-to-a-type-from-java-how-do-i-do-it
	 */
	@Override
	public void prepare(final Map stormConf, final TopologyContext context, final OutputCollector collector) {

		if (stormConf == null) {
			throw new IllegalArgumentException(
					"ElasticSearch configuration not found using key '" + this.configKey + "'");
		}

		Map<String, Object> conf = (Map<String, Object>) stormConf.get(this.configKey);

		esAsyncEnabled = Boolean.parseBoolean((String)conf.get(ES_ASYNC_ENABLED));
		String esClusterName = (String)conf.get(ES_CLUSTER_NAME);
		String esNodes = (String)conf.get(ES_NODES);
		boolean esShieldEnabled = Boolean.parseBoolean((String)conf.get(ES_SHIELD_ENABLED));
		String esShieldUser = (String)conf.get(ES_SHIELD_USER);
		String esShieldPass = (String)conf.get(ES_SHIELD_PASS);

		if (esClusterName == null) {
			throw new IllegalArgumentException("No '" + ES_CLUSTER_NAME
				+ "' value found in configuration!");
		}

		if (esNodes == null) {
			throw new IllegalArgumentException("No '" + ES_NODES
				+ "' value found in configuration!");
		}

		if (esShieldEnabled && esShieldUser == null) {
			throw new IllegalArgumentException("No '" + ES_SHIELD_USER
				+ "' value found in configuration!");
		}

		if (esShieldEnabled && esShieldPass == null) {
			throw new IllegalArgumentException("No '" + ES_SHIELD_PASS
				+ "' value found in configuration!");
		}

		this.collector = collector;

		// ElasticSearch 1.7
//		final Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", esClusterName).build();
//		TransportClient transportClient = new TransportClient(settings);

		// ElasticSearch 2.3
		if (esShieldEnabled) {
			final Settings settings = Settings.settingsBuilder()
					.put("cluster.name", esClusterName)
					.put("client.transport.sniff", true)
					.put("shield.user", esShieldUser + ":" + esShieldPass)
					.build();
			transportClient = TransportClient.builder().addPlugin(ShieldPlugin.class)
					.settings(settings).build();
		} else {
			final Settings settings = Settings.settingsBuilder()
					.put("cluster.name", esClusterName)
					.put("client.transport.sniff", true)
					.build();
			transportClient = TransportClient.builder().settings(settings).build();
		}

		synchronized (ESIndexerBolt.class) {
			if (client == null) {
				List<String> esNodesList = Arrays.asList(esNodes.split("\\s*,\\s*"));
				for (String esNode : esNodesList) {
					try {
						// ElasticSearch 1.7
//						transportClient.addTransportAddress(new InetSocketTransportAddress(esNode, 9300));
						// ElasticSearch 2.2
						transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esNode), 9300));
					} catch (Exception e) {
						LOG.warn("Unable to add ElasticSearch node: " + esNode);
					}
				}
				client = transportClient;
//				if (transportClient.connectedNodes().size() >= MIN_CONNECTED_NODES) {
//					client = transportClient;
//				} else {
//					transportClient.close();
//					throw new RuntimeException("Unable to initialize ElasticSearch client.");
//				}
			}
		}
	}

	/**
	 * http://storm.apache.org/documentation/Guaranteeing-message-processing.html
	 */
	@Override
	public void execute(Tuple tuple) {
		String sysID = (String) tuple.getValueByField(ApLogScheme.FIELD_SYS_ID);
		String logType = (String) tuple.getValueByField(ApLogScheme.FIELD_LOG_TYPE);
		String logDate = (String) tuple.getValueByField(ApLogScheme.FIELD_LOG_DATE);
		String apID = (String) tuple.getValueByField(ApLogScheme.FIELD_AP_ID);
		String at = (String) tuple.getValueByField(ApLogScheme.FIELD_AT);
		String msg = (String) tuple.getValueByField(ApLogScheme.FIELD_MSG);
		String toBeIndexed = (String) tuple.getValueByField(ApLogScheme.FIELD_ES_SOURCE);

		if (isNullOrEmpty(sysID) || isNullOrEmpty(logType) || isNullOrEmpty(logDate)
			|| !isDateValid(logDate, ApLogScheme.FORMAT_DATE) || isNullOrEmpty(apID)
			|| isNullOrEmpty(at) || isNullOrEmpty(msg) || isNullOrEmpty(toBeIndexed))
		{
			LOG.error("Received null or incorrect value from tuple.");
			collector.ack(tuple);
			return;
		}

		if (client == null) {
			collector.fail(tuple);
			throw new RuntimeException("Unable to get ES client!");
		}

		try {
			if (esAsyncEnabled) {
				// Asynchronous way
				ListenableActionFuture<IndexResponse> future = client
						.prepareIndex(ES_INDEX_PREFIX + sysID.toLowerCase()
							+ "-" + logDate, logType.toLowerCase())
						.setSource(toBeIndexed).execute();
				future.addListener(new ESIndexActionListener(tuple, collector, LOG));
				future.actionGet();
			} else {
				// Synchronous way
				IndexResponse response = client
						.prepareIndex(ES_INDEX_PREFIX + sysID.toLowerCase()
							+ "-" + logDate, logType.toLowerCase())
						.setSource(toBeIndexed).get();
				if (response == null) {
					LOG.error("Failed to index Tuple: {} ", tuple.toString());
				} else {
					if (response.isCreated()) {
						String documentId = response.getId();
						String logMsg = "Indexed successfully [" + sysID + "/"+ logType + "/" + documentId + "]";
						LOG.info(logMsg);
						LOG.debug(logMsg + " on Tuple: " + tuple.toString());
						// Anchored
						collector.emit(tuple, new Values(documentId));
					} else {
						LOG.error("Failed to index Tuple: {} ", tuple.toString());
					}
				}
			}
			collector.ack(tuple);
		} catch (ElasticsearchException ee) {
			// https://groups.google.com/forum/#!topic/storm-user/CGaKwFTa9TY
			ee.printStackTrace();
			collector.reportError(ee);
			collector.fail(tuple);
//			throw new ElasticsearchException("Unknown ElasticsearchException!");
		} catch (Exception e) {
			e.printStackTrace();
			collector.reportError(e);
			collector.fail(tuple);
//			throw new RuntimeException("Unknown Exception!");
//		} finally {
//			collector.ack(tuple);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("documentIndexId"));
	}

	@Override
	public void cleanup() {
		client.close();
	}

}
