/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/1.7/generate.html
 */
package com.pic.ala;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogScheme implements Scheme {

    TimeZone taipeiTimeZone = TimeZone.getTimeZone("GMT+8");

//	private static final long serialVersionUID = 7102546688047309944L;
//	private static final Logger LOG = LoggerFactory.getLogger(APLogScheme.class);
    private static final Logger LOG = Logger.getLogger(ApLogScheme.class);
//	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

	// The following fields will be used or stored by Elasticsearch.
	public static final String FIELD_ES_SOURCE = "es_source";	// ElasticSearch 物件的 source 欄位
	public static final String FIELD_SYSTEM_ID = "systemID";
	public static final String FIELD_LOG_DATE = "logDate";
	public static final String FIELD_LOG_TYPE = "logType";
	public static final String FIELD_LOG_TIME = "logTime";
	public static final String FIELD_AP_ID = "apID";
	public static final String FIELD_FUNCTION_ID = "functionID";
	public static final String FIELD_WHO = "who";
	public static final String FIELD_FROM = "from";
	public static final String FIELD_AT = "at";
	public static final String FIELD_TO = "to";
	public static final String FIELD_ACTION = "action";
	public static final String FIELD_RESULT = "result";
	public static final String FIELD_KEYWORD = "keyword";
	public static final String FIELD_MESSAGE_LEVEL = "messageLevel";
	public static final String FIELD_MESSAGE = "message";
	public static final String FIELD_MESSAGE_CODE = "messageCode";
	public static final String FIELD_TABLE_NAME = "tableName";
	public static final String FIELD_DATA_COUNT = "dataCount";

	// The following fields will be used or stored by HBase.
	private String counterColumnName;
	public static final String FIELD_AGG_ID = "aggID";
	public static final String FIELD_HOUR_MINUTE = "hourMinute";

	@Override
	public List<Object> deserialize(byte[] bytes) {
		try {

			// @TODO fix the following code to make it stabler!
			String logEntry = new String(bytes, "UTF-8");
			String[] pieces = logEntry.split("\\$\\$");

			String systemID = cleanup(pieces[0]);
			String logType = cleanup(pieces[1]);
			String logTimeString = cleanup(pieces[2]);
			DateTime logTime = dateTimeFormatter.parseDateTime(logTimeString);
			String apID = cleanup(pieces[3]);
			String functionID = cleanup(pieces[4]);
			String who = cleanup(pieces[5]);
			String from = cleanup(pieces[6]);
			String at = cleanup(pieces[7]);
			String to = cleanup(pieces[8]);
			String action = cleanup(pieces[9]);
			String result = cleanup(pieces[10]);
			String keyword = cleanup(pieces[11]);
			String messageLevel = cleanup(pieces[12]);
			String message = cleanup(pieces[13]);
			String messageCode = cleanup(pieces[14]);
			String tableName = cleanup(pieces[15]);
			String dataCount = cleanup(pieces[16]);

			LocalDate localDate = LocalDate.parse(logTimeString, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS"));
			String logDate = localDate.toString("yyyy-MM-dd");

			// The following fields are for HBase:
//			DateTime dateTime = DateTimeFormatter.parseDateTime(cleanup(pieces[2]));
//			long timestamp = System.currentTimeMillis();
//
//			int year = dateTime.getYear();
//			int month = dateTime.getMonthOfYear();
//			int day = dateTime.getDayOfMonth();
//			int hour = dateTime.getHourOfDay();
//			int minute = dateTime.getMinuteOfHour();
//
//			String hourMinute = String.valueOf(hour) + "-" + String.valueOf(minute);
//			String yearMonthDay = String.valueOf(year) + "-" + String.valueOf(month)
//									+ "-" + String.valueOf(day);
//			String aggId = yearMonthDay;
//
//			setCounterColumnName(hourMinute);

			// ElasticSearch 物件的 _source 欄位
			final XContentBuilder builder = jsonBuilder()
				    .startObject()
				        .field(FIELD_SYSTEM_ID, systemID)
				        .field(FIELD_LOG_TYPE, logType)
				        .field(FIELD_LOG_TIME, logTime)
				        .field(FIELD_AP_ID, apID)
				        .field(FIELD_FUNCTION_ID, functionID)
				        .field(FIELD_WHO, who)
				        .field(FIELD_FROM, from)
				        .field(FIELD_AT, at)
				        .field(FIELD_TO, to)
				        .field(FIELD_ACTION, action)
				        .field(FIELD_RESULT, result)
				        .field(FIELD_KEYWORD, keyword)
				        .field(FIELD_MESSAGE_LEVEL, messageLevel)
				        .field(FIELD_MESSAGE, message)
				        .field(FIELD_MESSAGE_CODE, messageCode)
				        .field(FIELD_TABLE_NAME, tableName)
				        .field(FIELD_DATA_COUNT, isNumeric(dataCount) ? Long.valueOf(dataCount) : dataCount)
				        .field("timestamp_ms", logTime.getMillis())
//				        .field("@timestamp", new Timestamp(System.currentTimeMillis()))
				        .field("@timestamp", logTime)
				    .endObject();

			return new Values(builder.string(), systemID, logType, logDate, logTime,
					apID, functionID, who, from, at, to, action, result,
					keyword, messageLevel, message, messageCode, tableName,
					dataCount);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public Fields getOutputFields() {
		return new Fields(FIELD_ES_SOURCE, FIELD_SYSTEM_ID, FIELD_LOG_TYPE,
				FIELD_LOG_DATE, FIELD_LOG_TIME, FIELD_AP_ID,
				FIELD_FUNCTION_ID, FIELD_WHO, FIELD_FROM, FIELD_AT,
				FIELD_TO, FIELD_ACTION, FIELD_RESULT, FIELD_KEYWORD,
				FIELD_MESSAGE_LEVEL, FIELD_MESSAGE, FIELD_MESSAGE_CODE,
				FIELD_TABLE_NAME, FIELD_DATA_COUNT);
	}

	private String cleanup(String str) {
		if (str != null) {
			return str.trim().replace("\n", "").replace("\t", "");
		} else {
			return str;
		}
	}

	public void setCounterColumnName(String counterColumnName) {
		this.counterColumnName = counterColumnName;
	}

	public String getCounterColumnName() {
		return this.counterColumnName;
	}

	/**
	 * 檢查某字串是否為整數？
	 *
	 * http://stackoverflow.com/questions/237159/whats-the-best-way-to-check-to-see-if-a-string-represents-an-integer-in-java
	 *
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	/**
	 * http://stackoverflow.com/questions/2563608/check-whether-a-string-is-parsable-into-long-without-try-catch
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
	    if (str == null) {
	        return false;
	    }
	    int sz = str.length();
	    if (sz == 0) {
	    	return false;
	    }
	    for (int i = 0; i < sz; i++) {
	        if (Character.isDigit(str.charAt(i)) == false) {
	            return false;
	        }
	    }
	    return true;
	}

	private static String dateToString(String str) {
		DateTime dt = dateTimeFormatter.parseDateTime(str);
		return dt.toString(fmt);
	}

}