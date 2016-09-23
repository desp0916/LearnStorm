package com.pic.ala.scheme;

import static com.pic.ala.util.LogUtil.parseDateTime;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class LogScheme implements Scheme {

	private static final Logger LOG = LoggerFactory.getLogger(LogScheme.class);

	public static final String FORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static final String FORMAT_DATE = "yyyy.MM.dd";

	private static final String[] FORMATS = new String[] {
		"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
		"yyyy-MM-dd HH:mm:ss.SSS",
		"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		"yyyy-MM-dd'T'HH:mm:ss.SSSZZ",
		"yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
	};

	public static final String FIELD_ES_SOURCE = "es_source";	// Elasticsearch "source" field
	public static final String FIELD_INDEX = "index";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_LOG_DATE = "logDate";
	public static final String FIELD_LOG_DATETIME = "logDateTime";
	public static final String FIELD_MESSAGE = "message";

	@Override
	public List<Object> deserialize(byte[] bytes) {

		String esSource = "";
		String index = "";
		String type = "";
		String message = "";
		String logDateTime = "";
		String logDate = "";

		try {
			esSource = new String(bytes, "UTF-8");

			ObjectMapper objectMapper = new ObjectMapper();
			Map<String,String> logEntry = objectMapper.readValue(esSource, Map.class);

			index = logEntry.get(FIELD_INDEX);
			type = logEntry.get(FIELD_TYPE);
			message = logEntry.get(FIELD_MESSAGE);

			String tmpLogDateTime = parseDateTime(logEntry.get("@timestamp"), FORMATS, FORMAT_DATETIME);
			String tmpLogDate = parseDateTime(logEntry.get("@timestamp"), FORMATS, FORMAT_DATE);

			if (tmpLogDateTime != null && tmpLogDate != null) {
				logDateTime = tmpLogDateTime;
				logDate = tmpLogDate;
			}

		} catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
		}

		return new Values(esSource, index, type, logDate, logDateTime, message);
	}

	@Override
	public Fields getOutputFields() {
		return new Fields(FIELD_ES_SOURCE, FIELD_INDEX, FIELD_TYPE,
				FIELD_LOG_DATE, FIELD_LOG_DATETIME, FIELD_MESSAGE);
	}

}