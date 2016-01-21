package com.pic.ala;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.codehaus.jackson.map.ObjectMapper;

import com.pic.ala.gen.ApLog;

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
	private final Random _rand = new Random();
	private ObjectMapper objectMapper;

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this._collector = collector;
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public void nextTuple() {
		try {
			Utils.sleep(ThreadLocalRandom.current().nextInt(1, 11) * 1000);
			String sysID = ApLog.getRandomOption(ApLog.SYSTEMS);
			String logType = ApLog.getRandomOption(ApLog.LOG_TYPES);
			ApLog log = new ApLog(sysID, logType);
//			_collector.emit(new Values(log.toString()));
			_collector.emit(new Values(objectMapper.writeValueAsString(log)));
		} catch (IOException e) {
			_collector.reportError(e);
		}
	}

	@Override
	public void ack(Object id) {
	}

	@Override
	public void fail(Object id) {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("log"));
	}

}