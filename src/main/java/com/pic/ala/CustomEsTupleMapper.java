package com.pic.ala;

import org.apache.storm.elasticsearch.common.EsTupleMapper;

import backtype.storm.tuple.ITuple;

public class CustomEsTupleMapper implements EsTupleMapper {

	private static final long serialVersionUID = -8845567154333943573L;

	public String getSource(ITuple tuple) {
		return (String) tuple.getValueByField(ApLogScheme.FIELD_ES_SOURCE);
	}

	public String getIndex(ITuple tuple) {
		return (String) tuple.getValueByField(ApLogScheme.FIELD_ES_INDEX_NAME);
	}

	public String getType(ITuple tuple) {
		return (String) tuple.getValueByField(ApLogScheme.FIELD_ES_INDEX_TYPE);
	}

	public String getId(ITuple tuple) {
		return null;
	}

}
