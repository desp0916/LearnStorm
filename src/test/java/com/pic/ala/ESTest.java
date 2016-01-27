package com.pic.ala;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ESTest {

	private static Client client;

	public static void main(String[] args) {

		String esNodesString = "hdp01.localdomain,hdp02.localdomain,hdp03.localdomain,hdp04.localdomain,hdp05.localdomain";
		List<String> esNodesList = Arrays.asList(esNodesString.split("\\s*,\\s*"));
//		List<InetSocketTransportAddress> esNodes = new ArrayList<InetSocketTransportAddress>();

		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
		TransportClient transportClient = new TransportClient(settings);

		for (String esNode : esNodesList) {
			transportClient.addTransportAddress(new InetSocketTransportAddress(esNode, 9300));
		}

		for (DiscoveryNode dNode: transportClient.connectedNodes()) {
			System.out.println(dNode.toString());
		}

		client.prepareIndex();

		client.close();
	}
}