/**
 * Index the log stream in asynchronous way.
 */
package com.pic.ala.bolt;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;

import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ESIndexActionListener implements ActionListener<IndexResponse> {

	private final Tuple tuple;
	private final OutputCollector collector;
	private final Logger logger;

	public ESIndexActionListener(Tuple tuple, OutputCollector collector, Logger logger) {
		super();
		this.tuple = tuple;
		this.collector = collector;
		this.logger = logger;
	}

	@Override
	public void onResponse(IndexResponse response) {
		if (response.isCreated()) {
			String index = response.getIndex();
			String type = response.getType();
			String documentId = response.getId();
			String logMsg = "Indexed successfully [" + index + "/"+ type + "/" + documentId + "]";
			logger.info(logMsg);
			logger.debug(logMsg + " on Tuple: " + tuple.toString());
			// Anchored
			collector.emit(tuple, new Values(documentId));
		} else {
			logger.error("Failed to index Tuple: {} ", tuple.toString());
		}
	}

	@Override
	public void onFailure(Throwable e) {
		logger.error("Failed to index Tuple: {} ", tuple.toString());

	}

}