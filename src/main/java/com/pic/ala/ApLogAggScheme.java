/**
 * Converting a date string to a DateTime object using Joda Time library
 * http://stackoverflow.com/questions/6252678/converting-a-date-string-to-a-datetime-object-using-joda-time-library
 *
 * 有关java中的Date,String,Timestamp之间的转化问题
 * http://cjjwzs.iteye.com/blog/984818
 *
 */
package com.pic.ala;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogAggScheme implements Scheme {

	public static final String TABLE_NAME = "AES3G-TopicMinutely";	// HBase table name
	private static final Logger LOG = Logger.getLogger(ApLogAggScheme.class);

	private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static final long serialVersionUID = 6790774238070955812L;

	public static final String FIELD_ROWKEY = "TopicMinutelyAggId";
	public static String FIELD_COUNTER = "";

//	public static final String FIELD_COUNTER_NAME = "";
//	public static final int FIELD_COUNTER_VALUE = 1;

	public List<Object> deserialize(byte[] bytes) {
		try {
			String LogEntry = new String(bytes, "UTF-8");
			String[] pieces = LogEntry.split("\\$\\$");

			DateTime dateTime = formatter.parseDateTime(cleanup(pieces[2]));

			int year = dateTime.getYear();
			int month = dateTime.getMonthOfYear();
			int day = dateTime.getDayOfMonth();

			String rowKey = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day);

			int hour = dateTime.getHourOfDay();
			int minute = dateTime.getMinuteOfHour();

			String counterFieldName = String.valueOf(hour) + ":" + String.valueOf(minute);

			return new Values(rowKey, counterFieldName);

		} catch (UnsupportedEncodingException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	public Fields getOutputFields() {
		return new Fields(FIELD_ROWKEY, FIELD_COUNTER);
	}

	private String cleanup(String str) {
		if (str != null) {
			return str.trim().replace("\n", "").replace("\t", "");
		} else {
			return str;
		}
	}
}
