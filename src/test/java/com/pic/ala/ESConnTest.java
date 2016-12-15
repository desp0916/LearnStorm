package com.pic.ala;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.shield.ShieldPlugin;

// ES 1.7.4
//import org.elasticsearch.common.settings.ImmutableSettings;

public class ESConnTest {

	private static Client client;
	private static TransportClient transportClient;
	private static final boolean ES_SHIELD_ENABLED = true;

	public static void main(String[] args) {

		String esNodesString = "hdpr01wn01,hdpr01wn02,hdpr01wn03,hdpr01wn04,hdpr01wn05";
		List<String> esNodesList = Arrays.asList(esNodesString.split("\\s*,\\s*"));

//		List<InetSocketTransportAddress> esNodes = new ArrayList<InetSocketTransportAddress>();

		// ES 1.7
//		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
//		TransportClient transportClient = new TransportClient(settings);

		// ES 2.2
//		final Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
//		TransportClient transportClient = TransportClient.builder().build();
		if (ES_SHIELD_ENABLED) {
			final Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch")
					.put("client.transport.sniff", true).put("shield.user", "transport_client_user:aploganalyzerpass").build();
			transportClient = TransportClient.builder().addPlugin(ShieldPlugin.class)
					.settings(settings).build();
		} else {
			final Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch")
					.put("client.transport.sniff", true).build();
			transportClient = TransportClient.builder().settings(settings).build();
		}

		for (String esNode : esNodesList) {
			// ES 1.7
//			transportClient.addTransportAddress(new InetSocketTransportAddress(esNode, 9300));
			try {
//				 ES 2.2
				transportClient
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esNode), 9300));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

		for (DiscoveryNode dNode: transportClient.connectedNodes()) {
			System.out.println(dNode.toString());
		}

		client = transportClient;
		String indexName  = "aplog_aes3g-2016.04.12";
		String indexType = "ui";

		String toBeIndexed = "{\"sysID\":\"wds\",\"logType\":\""+indexType+"\",\"logTime\":\"2016-04-12T16:51:31.924+0800\",\"apID\":\"UIApp01V4\",\"functID\":\"FUNC_10002\",\"who\":\"機器人\",\"from\":\"iis\",\"at\":\"websphere\",\"to\":\"postgres\",\"action\":\"訂單成立\",\"result\":\"失敗\",\"kw\":\"玩命關頭\",\"msgLevel\":\"ERROR\",\"msg\":\"Unsufficient privilege\",\"msgCode\":\"5260\",\"table\":\"CODES\",\"dataCnt\":176,\"procTime\":80}";
		IndexResponse response = client
				.prepareIndex(indexName, indexType)
				.setSource(toBeIndexed).get();

		if (response.isCreated()) {
			String documentIndexId = response.getId();
			// Anchored
			System.out.println("OK. The documentIndexId: " + documentIndexId);
		} else {
			System.out.println("FAILED");
		}

		client.close();
	}
}