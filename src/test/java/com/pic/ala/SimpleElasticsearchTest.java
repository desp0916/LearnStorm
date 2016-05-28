package com.pic.ala;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.elasticsearch.action.get.GetResponse;
import org.junit.Test;

/**
 * Demonstrates how to use an embedded elasticsearch server in your tests.
 *
 * You'd better use Java 8 or above to run the following code, or you will get
 * the error message: 'java : Unsupported major.minor version 52.0'.
 * http://stackoverflow.com/questions/23249331/java-unsupported-major-minor-
 * version-52-0
 *
 * @author Felix Müller
 */

public class SimpleElasticsearchTest extends AbstractElasticsearchIntegrationTest {

	@Test
	public void indexSimpleDocument() throws IOException {

		getClient().prepareIndex("myindex", "document", "1")
				.setSource(jsonBuilder().startObject().field("test", "123").endObject()).execute().actionGet();

		GetResponse fields = getClient().prepareGet("myindex", "document", "1").execute().actionGet();
		assertThat(fields.getSource().get("test")).isEqualTo("123");
	}
}
