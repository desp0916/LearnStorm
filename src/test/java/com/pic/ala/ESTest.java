package com.pic.ala;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

// ES 1.7.4
//import org.elasticsearch.common.settings.ImmutableSettings;

public class ESTest {

	private static Client client;

	public static void main(String[] args) {

		String esNodesString = "hdp01.localdomain,hdp02.localdomain,hdp03.localdomain,hdp04.localdomain,hdp05.localdomain";
		List<String> esNodesList = Arrays.asList(esNodesString.split("\\s*,\\s*"));
//		List<InetSocketTransportAddress> esNodes = new ArrayList<InetSocketTransportAddress>();

		// ES 1.7
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
		TransportClient transportClient = new TransportClient(settings);

		// ES 2.2
//		final Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
//		TransportClient transportClient = TransportClient.builder().build();

		for (String esNode : esNodesList) {
			// ES 1.7
			transportClient.addTransportAddress(new InetSocketTransportAddress(esNode, 9300));
//			try {
				// ES 2.2
//				transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esNode), 9300));
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			}
		}

		for (DiscoveryNode dNode: transportClient.connectedNodes()) {
			System.out.println(dNode.toString());
		}
		client = transportClient;
		client.prepareIndex();
		client.close();
	}
}