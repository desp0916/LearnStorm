/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/1.7/generate.html
 * https://www.elastic.co/guide/en/elasticsearch/reference/1.7/mapping-date-format.html#built-in-date-formats
 *
 * @TODO Discard undefined fields in ApLog.
 */
package com.pic.ala.scheme;

import static com.pic.ala.util.LogUtil.isNumeric;
import static com.pic.ala.util.LogUtil.parseDateTime;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pic.ala.model.ApLog;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogScheme implements Scheme {

    private static final Logger LOG = LoggerFactory.getLogger(ApLogScheme.class);
	public static final String FORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static final String FORMAT_DATE = "yyyy.MM.dd";
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(FORMAT_DATETIME);
	private static final String[] FORMATS = new String[] {
			"yyyy-MM-dd HH:mm:ss.SSS",			
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ" };

	// The following fields will be used or stored by Elasticsearch.
	public static final String FIELD_ES_SOURCE = "es_source";	// ElasticSearch 物件的 source 欄位
	public static final String FIELD_SYS_ID = "sysID";
	public static final String FIELD_LOG_DATE = "logDate";
	public static final String FIELD_LOG_TYPE = "logType";
	public static final String FIELD_LOG_DATETIME = "logDateTime";
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
	public static final String FIELD_AGG_ID = "aggID";
	public static final String FIELD_HOUR_MINUTE = "hourMinute";
	public static final String FIELD_ROWKEY = "rowKey";

	/**
	 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
	 */
	@Override
	public List<Object> deserialize(byte[] bytes) {

		String esSource = "";
		String sysID = "";
		String logType = "";
		String apID = "";
		String at = "";
		String msg = "";
		String logDateTime = "";
		String logDate = "";

		try {
			esSource = new String(bytes, "UTF-8");

			ObjectMapper objectMapper = new ObjectMapper();
			ApLog apLog = objectMapper.readValue(esSource, ApLog.class);

			sysID = apLog.getSysID();
			logType = apLog.getLogType();
			apID = apLog.getApID();
			at = apLog.getAt();
			msg = apLog.getMsg();
			String tmpLogDateTime = parseDateTime(apLog.getLogTime(), dateTimeFormatter, FORMATS, FORMAT_DATETIME);
			String tmpLogDate = parseDateTime(apLog.getLogTime(), dateTimeFormatter, FORMATS, FORMAT_DATE);
			
			LOG.error("*******apLog.getLogTime(): {}", apLog.getLogTime());
			LOG.error("#######tmpLogDate: {}", logDate);
			
			if (tmpLogDateTime != null && tmpLogDate != null) {
				logDateTime = tmpLogDateTime;
				logDate = tmpLogDate;
			}

		}  catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
//			throw new RuntimeException(e);
		}

		return new Values(esSource, sysID, logType, logDate, logDateTime, apID, at, msg);
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
				        .field(FIELD_LOG_DATETIME, logTime)
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
		return new Fields(FIELD_ES_SOURCE, FIELD_SYS_ID, FIELD_LOG_TYPE,
				FIELD_LOG_DATE, FIELD_LOG_DATETIME, FIELD_AP_ID, FIELD_AT,
				FIELD_MSG);
	}

	private String cleanup(String str) {
		if (str != null) {
			return str.trim().replace("\n", "").replace("\t", "");
		} else {
			return str;
		}
	}

}