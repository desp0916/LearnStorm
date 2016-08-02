package com.pic.ala.spout;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.codehaus.jackson.map.ObjectMapper;

import com.pic.ala.model.ApLog;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

public class RandomLogSpout extends BaseRichSpout {

	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector _collector;
	private ObjectMapper objectMapper;
    // _pending key = Kafka offset, value = time at which the message was first submitted to the topology
    private SortedMap<String, Long> _pending = new TreeMap<String, Long>();

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this._collector = collector;
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public void nextTuple() {
		try {
			Utils.sleep(ThreadLocalRandom.current().nextInt(1, 11) * 1000);
			final String sysID = ApLog.getRandomOption(ApLog.SYSTEMS);
			final String logType = ApLog.getRandomOption(ApLog.LOG_TYPES);
			final ApLog log = new ApLog(sysID, logType);
			String msgId = UUID.randomUUID().toString();
			_pending.put(msgId, System.currentTimeMillis());
			_collector.emit(new Values(objectMapper.writeValueAsString(log)), msgId);
		} catch (IOException e) {
			_collector.reportError(e);
		}
	}

	@Override
	public void ack(Object msgId) {
		_pending.remove(msgId);
	}

	@Override
	public void fail(Object msgId) {
		// Just remove the pending/failed tuple.
		_pending.remove(msgId);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("log"));
	}

}