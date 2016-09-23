/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/1.7/generate.html
 * https://www.elastic.co/guide/en/elasticsearch/reference/1.7/mapping-date-format.html#built-in-date-formats
 *
 * @TODO Discard undefined fields in ApLog.
 */
package com.pic.ala.scheme;

import static com.pic.ala.util.LogUtil.parseDateTime;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pic.ala.model.ApLog;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogScheme implements Scheme {

    private static final Logger LOG = LoggerFactory.getLogger(ApLogScheme.class);
	public static final String FORMAT_DATE = "yyyy.MM.dd";
	private static final String[] FORMATS = new String[] {
			"yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
			"yyyy-MM-dd'T'HH:mm:ss.SSSZZ",
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			"yyyy-MM-dd HH:mm:ss.SSS",
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	};

	// The following fields will be used or stored by Elasticsearch.
	public static final String FIELD_ES_SOURCE = "es_source";	// ElasticSearch 物件的 source 欄位
	public static final String FIELD_SYS_ID = "sysID";
	public static final String FIELD_LOG_DATE = "logDate";
	public static final String FIELD_LOG_TYPE = "logType";
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
			String tmpLogDate = parseDateTime(apLog.getLogTime(), FORMATS, FORMAT_DATE);

			LOG.error("*******apLog.getLogTime(): {}", apLog.getLogTime());
			LOG.error("#######tmpLogDate: {}", logDate);

			if (tmpLogDate != null) {
				logDate = tmpLogDate;
			}

		}  catch (Exception e) {
			LOG.error(e.getMessage());
			e.printStackTrace();
//			throw new RuntimeException(e);
		}

		return new Values(esSource, sysID, logType, logDate, apID, at, msg);
	}

	@Override
	public Fields getOutputFields() {
		return new Fields(FIELD_ES_SOURCE, FIELD_SYS_ID, FIELD_LOG_TYPE,
				FIELD_LOG_DATE, FIELD_AP_ID, FIELD_AT, FIELD_MSG);
	}


}