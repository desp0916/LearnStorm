/**
 * Embedded Elasticsearch Server for Tests
 * http://cupofjava.de/blog/2012/11/27/embedded-elasticsearch-server-for-tests/
 */

package com.pic.ala;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class EmbeddedElasticsearchServer {

	private static final String CLUSTER_NAME = "elasticsearch";
	private static final String NODE_NAME = "es-embedded";
	private static final String PATH_HOME = "target/es-embedded";
	private static final String PATH_DATA = "target/es-embedded/data";

	private final String clusterName;
	private final String nodeName;
	private final String pathHome;
	private final String pathData;

	private final Node node;

	public EmbeddedElasticsearchServer() {
		this(CLUSTER_NAME, NODE_NAME, PATH_HOME, PATH_DATA);
	}

	public EmbeddedElasticsearchServer(String clusterName, String nodeName, String pathHome, String pathData) {

		this.clusterName = clusterName;
		this.nodeName = nodeName;
		this.pathHome = pathHome;
		this.pathData = pathData;

		Settings.Builder settings = Settings.settingsBuilder()
				.put("http.enabled", false)
				.put("node.name", nodeName)
				.put("path.home", pathHome)
				.put("path.data", pathData);

		node = NodeBuilder.nodeBuilder()
				.local(true).data(true).clusterName(clusterName)
				.settings(settings.build()).node();
	}

	public Client getClient() {
		return node.client();
	}

	public void shutdown() {
		node.close();
		deleteDataDirectory();
	}

	private void deleteDataDirectory() {
		try {
			FileUtils.deleteDirectory(new File(pathData));
		} catch (IOException e) {
			throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
		}
	}

	public static void main(String arags[]) {
		EmbeddedElasticsearchServer.main(arags);
	}
}