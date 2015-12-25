package com.pic.ala;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.sql.Timestamp;
import java.util.List;

//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
import org.apache.storm.joda.time.DateTime;
import org.apache.storm.joda.time.format.DateTimeFormat;
import org.apache.storm.joda.time.format.DateTimeFormatter;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogScheme implements Scheme {

	private static final Logger LOG = LoggerFactory.getLogger(ApLogScheme.class);

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private String counterColumnName;

	private static final long serialVersionUID = -578815753542323978L;

	public static final String SYSTEM_ID = "aes3g"; // HBase Table name
	public static final String FIELD_ES_SOURCE = "es_source";
	public static final String FIELD_ES_INDEX_NAME = "es_index_name";	// ElasticSearch Index name
	public static final String FIELD_ES_INDEX_TYPE = "es_index_type";	// ElasticSearch Index type
	public static final String ES_INDEX = SYSTEM_ID;
	public static final String LOG_TYPE = "batch";

	public static final String LOG_JSON = "{}";

	public static final String FIELD_LOG_ID = "apLogId";
	public static final String FIELD_HOSTIP = "hostIP";
	public static final String FIELD_AP_ID = "apId";
	public static final String FIELD_LOG_TIME = "logTime";
	public static final String FIELD_LOG_LEVEL = "errLevel";
	public static final String FIELD_CLASS_METHOD = "classMethod";
	public static final String FIELD_KEYWORD1 = "keyword1";
	public static final String FIELD_KEYWORD2 = "keyword2";
	public static final String FIELD_KEYWORD3 = "keyword3";
	public static final String FIELD_MESSAGE = "message";

	public static final String AGG_TABLE = "aes3g_agg"; // HBase Table name
	public static final String FIELD_AGG_ID = "aggId";
	public static final String FIELD_HOUR_MINUTE = "hourMinute";

	public void setCounterColumnName(String counterColumnName) {
		this.counterColumnName = counterColumnName;
	}

	public String getCounterColumnName() {
		return this.counterColumnName;
	}

	class TimestampJSON {
		protected boolean enabled = true;
		TimestampJSON() {
			enabled = true;
		}
	}

	public List<Object> deserialize(byte[] bytes) {
		try {

			String logEntry = new String(bytes, "UTF-8");
			String[] pieces = logEntry.split("\\$\\$");
			// aes3g-AESRCT1-AES-job-ERROR-2015-01-05 10:50:31,346

			String hostIP = cleanup(pieces[0]);
			String apId = cleanup(pieces[1]);
			String logTime = cleanup(pieces[2]);
			String logLevel = cleanup(pieces[3]);
			String classMethod = cleanup(pieces[4]);
			String keyword1 = cleanup(pieces[5]);
			String keyword2 = cleanup(pieces[6]);
			String keyword3 = cleanup(pieces[7]);
			String message = cleanup(pieces[8]);
			String logId = SYSTEM_ID + "-" + hostIP + "-" + apId + "-" + LOG_TYPE + "-" + classMethod + "-" + logTime;

			//
			// @TODO fix the following code to make it stabler!

			DateTime dateTime = formatter.parseDateTime(cleanup(pieces[2]));
			long timestamp = System.currentTimeMillis();

			int year = dateTime.getYear();
			int month = dateTime.getMonthOfYear();
			int day = dateTime.getDayOfMonth();
			int hour = dateTime.getHourOfDay();
			int minute = dateTime.getMinuteOfHour();

			String hourMinute = String.valueOf(hour) + "-" + String.valueOf(minute);
			String yearMonthDay = String.valueOf(year) + "-" + String.valueOf(month)
									+ "-" + String.valueOf(day);
			String aggId = yearMonthDay;

			setCounterColumnName(hourMinute);

			XContentBuilder builder = jsonBuilder()
				    .startObject()
				        .field("systemId", SYSTEM_ID)
				        .field("logTime", logTime)
				        .field("logLevel", logLevel)
				        .field("classMethod", classMethod)
				        .field("hostIP", hostIP)
				        .field("keyword1", keyword1)
				        .field("keyword2", keyword2)
				        .field("keyword3", keyword3)
				        .field("message", message)
				        .field("timestamp_ms", System.currentTimeMillis())
				        .field("@timestamp", new Timestamp(timestamp))
				    .endObject();

//			logJsonObj.put("messageId", messageId);
//			logJsonObj.put("systemId", SYSTEM_ID);
//			logJsonObj.put("logTime", logTime);
//			logJsonObj.put("logLevel", logLevel);
//			logJsonObj.put("classMethod", classMethod);
//			logJsonObj.put("hostIP", hostIP);
////			logJsonObj.put("appId", value);
////			logJsonObj.put("action", value);
////			logJsonObj.put("functionId", value);
////			logJsonObj.put("result", value);
//			logJsonObj.put("keyword1", keyword1);
//			logJsonObj.put("keyword2", keyword2);
//			logJsonObj.put("keyword3", keyword3);
//			logJsonObj.put("message", message);
////			logJsonObj.put("dataCount", value);
//
//			logJsonObj.put("timestamp_ms", System.currentTimeMillis());
//			logJsonObj.put("@timestamp", new Timestamp(timestamp));

			return new Values(builder.string(), ES_INDEX, LOG_TYPE, logId,
					hostIP, logTime, logLevel, classMethod, keyword1, keyword2,
					keyword3, message, aggId, hourMinute);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public Fields getOutputFields() {
		return new Fields(FIELD_ES_SOURCE, FIELD_ES_INDEX_NAME, FIELD_ES_INDEX_TYPE,
				FIELD_LOG_ID, FIELD_HOSTIP, FIELD_LOG_TIME, FIELD_LOG_LEVEL,
				FIELD_CLASS_METHOD, FIELD_KEYWORD1,	FIELD_KEYWORD2, FIELD_KEYWORD3,
				FIELD_MESSAGE, FIELD_AGG_ID, FIELD_HOUR_MINUTE);
	}

	private String cleanup(String str) {
		if (str != null) {
			return str.trim().replace("\n", "").replace("\t", "");
		} else {
			return str;
		}
	}

}
