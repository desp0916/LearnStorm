/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/1.7/generate.html
 * https://www.elastic.co/guide/en/elasticsearch/reference/1.7/mapping-date-format.html#built-in-date-formats
 */
package com.pic.ala;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pic.ala.gen.ApLog;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogScheme implements Scheme {

	private static final long serialVersionUID = 1L;

	private static final String[] FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
			"yyyy-MM-dd HH:mm:ss.SSS",
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ" };

//	TimeZone taipeiTimeZone = TimeZone.getTimeZone("GMT+8");

//	private static final long serialVersionUID = 7102546688047309944L;
//	private static final Logger LOG = LoggerFactory.getLogger(APLogScheme.class);
    private static final Logger LOG = LoggerFactory.getLogger(ApLogScheme.class);
//	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

	// The following fields will be used or stored by Elasticsearch.
	public static final String FIELD_ES_SOURCE = "es_source";	// ElasticSearch 物件的 source 欄位
	public static final String FIELD_SYS_ID = "sysID";
	public static final String FIELD_LOG_DATE = "logDate";
	public static final String FIELD_LOG_TYPE = "logType";
	public static final String FIELD_LOG_TIME = "logTime";
	public static final String FIELD_AP_ID = "apID";
	public static final String FIELD_FUNCT_ID = "functID";
	public static final String FIELD_WHO = "who";
	public static final String FIELD_FROM = "from";
	public static final String FIELD_AT = "at";
	public static final String FIELD_TO = "to";
	public static final String FIELD_ACTION = "action";
	public static final String FIELD_RESULT = "result";
	public static final String FIELD_KW = "kw";
	public static final String FIELD_MSG_LEVEL = "msgLevel";
	public static final String FIELD_MSG = "msg";
	public static final String FIELD_MSG_CODE = "msgCode";
	public static final String FIELD_TABLE = "table";
	public static final String FIELD_DATA_CNT = "dataCnt";
	public static final String FIELD_PROC_TIME = "procTime";

	// The following fields will be used or stored by HBase.
	private String counterColumnName;
	public static final String FIELD_AGG_ID = "aggID";
	public static final String FIELD_HOUR_MINUTE = "hourMinute";
	public static final String FIELD_ROWKEY = "rowKey";

	/**
	 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
	 */
	@Override
	public List<Object> deserialize(byte[] bytes) {
		try {
			String esSource = new String(bytes, "UTF-8");

			ObjectMapper objectMapper = new ObjectMapper();
			ApLog apLog = objectMapper.readValue(esSource, ApLog.class);
			String sysID = apLog.getSysID();
			String logType = apLog.getLogType();
			Date logTime = parseDate(apLog.getLogTime());
			// Multiple patterns:
//			LocalDate localDate = LocalDate.parse(logTime, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
//			String logDate = localDate.toString("yyyy-MM-dd");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String logDate = sdf.format(logTime);
			String apID = apLog.getApID();
//			String functID = apLog.getFunctID();
//			String who = apLog.getWho();
//			String from = apLog.getFrom();
			String at = apLog.getAt();
//			String to = apLog.getTo();
//			String action = apLog.getAction();
//			String result = apLog.getResult();
//			String keyword = apLog.getKeyword();
//			String msgLevel = apLog.getMsgLevel();
			String msg = apLog.getMsg();
//			String msgCode = apLog.getMsgCode();
//			String table = apLog.getTable();
//			int dataCnt = apLog.getDataCnt();
//			int procTime = apLog.getProcTime();

//			return new Values(esSource, sysID, logType, logDate, logTime,
//					apID, functID, who, from, at, to, action, result,
//					kw, msgLevel, msg, msgCode, table,
//					dataCnt, procTime);

			return new Values(esSource, sysID, logType, logDate, logTime, apID, at, msg);

		}  catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
//			throw new RuntimeException(e);
			return new Values("", "", "", "", "");
		}
	}

	public List<Object> deserializeOld(byte[] bytes) {
		try {
			// @TODO fix the following code to make it stabler!
			String logEntry = new String(bytes, "UTF-8");
			String[] pieces = logEntry.split("\\$\\$");

			String sysID = cleanup(pieces[0]);
			String logType = cleanup(pieces[1]);
			String logTimeString = cleanup(pieces[2]);
			DateTime logTime = dateTimeFormatter.parseDateTime(logTimeString);
			String apID = cleanup(pieces[3]);
			String functID = cleanup(pieces[4]);
			String who = cleanup(pieces[5]);
			String from = cleanup(pieces[6]);
			String at = cleanup(pieces[7]);
			String to = cleanup(pieces[8]);
			String action = cleanup(pieces[9]);
			String result = cleanup(pieces[10]);
			String keyword = cleanup(pieces[11]);
			String msgLevel = cleanup(pieces[12]);
			String msg = cleanup(pieces[13]);
			String msgCode = cleanup(pieces[14]);
			String table = cleanup(pieces[15]);
			String dataCnt = cleanup(pieces[16]);
			String procTime = cleanup(pieces[17]);

			LocalDate localDate = LocalDate.parse(logTimeString, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS"));
			String logDate = localDate.toString("yyyy-MM-dd");

			// The following fields are for HBase:

//			String rowKey = sysID + apID + functID +;

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
				        .field(FIELD_SYS_ID, sysID)
				        .field(FIELD_LOG_TYPE, logType)
				        .field(FIELD_LOG_TIME, logTime)
				        .field(FIELD_AP_ID, apID)
				        .field(FIELD_FUNCT_ID, functID)
				        .field(FIELD_WHO, who)
				        .field(FIELD_FROM, from)
				        .field(FIELD_AT, at)
				        .field(FIELD_TO, to)
				        .field(FIELD_ACTION, action)
				        .field(FIELD_RESULT, result)
				        .field(FIELD_KW, keyword)
				        .field(FIELD_MSG_LEVEL, msgLevel)
				        .field(FIELD_MSG, msg)
				        .field(FIELD_MSG_CODE, msgCode)
				        .field(FIELD_TABLE, table)
				        .field(FIELD_DATA_CNT, isNumeric(dataCnt) ? Integer.valueOf(dataCnt) : dataCnt)
				        .field(FIELD_PROC_TIME, isNumeric(procTime) ? Integer.valueOf(procTime) : procTime)
				        .field("timestamp_ms", logTime.getMillis())
//				        .field("@timestamp", new Timestamp(System.currentTimeMillis()))
				        .field("@timestamp", logTime)
				    .endObject();

			return new Values(builder.string(), sysID, logType, logDate, logTime,
					apID, functID, who, from, at, to, action, result,
					keyword, msgLevel, msg, msgCode, table,
					dataCnt, procTime);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public Fields getOutputFields() {
		// Required: esSource, systemID, logType, logDate, logTime
//		return new Fields(FIELD_ES_SOURCE, FIELD_SYS_ID, FIELD_LOG_TYPE,
//				FIELD_LOG_DATE, FIELD_LOG_TIME, FIELD_AP_ID,
//				FIELD_FUNCT_ID, FIELD_WHO, FIELD_FROM, FIELD_AT,
//				FIELD_TO, FIELD_ACTION, FIELD_RESULT, FIELD_KW,
//				FIELD_MSG_LEVEL, FIELD_MSG, FIELD_MSG_CODE,
//				FIELD_TABLE, FIELD_DATA_CNT);
		return new Fields(FIELD_ES_SOURCE, FIELD_SYS_ID, FIELD_LOG_TYPE,
				FIELD_LOG_DATE, FIELD_LOG_TIME, FIELD_AP_ID, FIELD_AT, FIELD_MSG);
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

	private static Date parseDate(String value) {
		for (int i = 0; i < FORMATS.length; i++) {
			SimpleDateFormat format = new SimpleDateFormat(FORMATS[i]);
			Date temp;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			try {
				temp = format.parse(value);
				if (temp != null)
					return temp;
			} catch (ParseException e) {
			}
		}
		LOG.error("Could not parse timestamp for log");
		return null;
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