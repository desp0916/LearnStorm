package com.pic.ala;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LogBaseTopology {

    private static final Logger LOG = LoggerFactory.getLogger(LogBaseTopology.class);

	protected static Properties topologyConfig;

	public LogBaseTopology(String configFileLocation) throws Exception {

		topologyConfig = new Properties();

		try {
			topologyConfig.load(ClassLoader.getSystemResourceAsStream(configFileLocation));
		} catch (FileNotFoundException e) {
			LOG.error("Encountered error while reading configuration properties: "
					+ e.getMessage());
			throw e;
		} catch (IOException e) {
			LOG.error("Encountered error while reading configuration properties: "
					+ e.getMessage());
			throw e;
		}
	}
}
