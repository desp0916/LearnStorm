package com.pic.ala;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

public class ApLogScheme implements Scheme {

	private static final long serialVersionUID = -578815753542323978L;

	public static final String SYSTEM_ID = "aes3g";
	public static final String AP_ID = "data_exchage";
	public static final String LOG_TYPE = "job";

	public static final String FIELD_LOG_ID = "apLogId";
	public static final String FIELD_HOSTNAME = "hostName";
	public static final String FIELD_EXEC_TIME = "execTime";
	public static final String FIELD_ERROR_LEVEL = "errLevel";
	public static final String FIELD_EXEC_METHOD = "execMethod";
	public static final String FIELD_KEYWORD1 = "keyword1";
	public static final String FIELD_KEYWORD2 = "keyword2";
	public static final String FIELD_KEYWORD3 = "keyword3";
	public static final String FIELD_MESSAGE = "message";

	private static final Logger LOG = Logger.getLogger(ApLogScheme.class);

	public List<Object> deserialize(byte[] bytes) {
		try {
			String LogEntry = new String(bytes, "UTF-8");
			String[] pieces = LogEntry.split("\\$\\$");

			Timestamp execTimeStamp = Timestamp.valueOf(cleanup(pieces[1]));
			String hostName = cleanup(pieces[0]);
			String execTime = cleanup(pieces[1]);
			String errLevel = cleanup(pieces[2]);
			String execMethod = cleanup(pieces[3]);
			String keyword1 = cleanup(pieces[4]);
			String keyword2 = cleanup(pieces[5]);
			String keyword3 = cleanup(pieces[6]);
			String message = cleanup(pieces[7]);
			String logId = SYSTEM_ID + "-" + AP_ID + "-" + LOG_TYPE + "-" + hostName
							+ "-" + execMethod + "-" + execTimeStamp.toString();

			return new Values(logId, hostName, execTime, errLevel, execMethod, keyword1, keyword2, keyword3, message);

		} catch (UnsupportedEncodingException e) {
			LOG.error(e);
			throw new RuntimeException(e);
		}
	}

	public Fields getOutputFields() {
		return new Fields(
				FIELD_LOG_ID, FIELD_HOSTNAME, FIELD_EXEC_TIME,
				FIELD_ERROR_LEVEL, FIELD_EXEC_METHOD,
				FIELD_KEYWORD1, FIELD_KEYWORD2, FIELD_KEYWORD3,
				FIELD_MESSAGE);
	}

	private String cleanup(String str) {
		if (str != null) {
			return str.trim().replace("\n", "").replace("\t", "");
		} else {
			return str;
		}
	}

}
