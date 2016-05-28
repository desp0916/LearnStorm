/**
 * Embedded Elasticsearch Server for Tests
 * http://cupofjava.de/blog/2012/11/27/embedded-elasticsearch-server-for-tests/
 */

package com.pic.ala;

import org.elasticsearch.client.Client;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractElasticsearchIntegrationTest {

	private EmbeddedElasticsearchServer embeddedElasticsearchServer;

	@Before
	public void startEmbeddedElasticsearchServer() {
		embeddedElasticsearchServer = new EmbeddedElasticsearchServer();
	}

	@After
	public void shutdownEmbeddedElasticsearchServer() {
		embeddedElasticsearchServer.shutdown();
	}

	protected Client getClient() {
		return embeddedElasticsearchServer.getClient();
	}
}
