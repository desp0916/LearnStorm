package com.pic.ala;

import java.util.List;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;

public class LogEntry implements Scheme {

	private static final long serialVersionUID = -6533000977961490464L;
	private static final Logger LOG = LoggerFactory.getLogger(LogEntry.class);

	private String messageId;	// 紀錄識別		（UI & Batch & TPIPAS）
	private String systemId;	// 系統			（Batch）
	private String logTime;		// Log 寫入時間	（UI & Batch）
	private String logLevel;	// 訊息層級		（Batch）
	private String classMethod;	// 執行程式		（Batch）
	private String hostIP;		// 本地端			（UI & TPIPAS）
	private String appId;		// 人			（UI & TPIPAS）
	private String action;		// 執行動作		（UI & TPIPAS）
	private String functionId;	// 功能代號		（UI & TPIPAS）
	private String result;		// 執行結果		（UI & TPIPAS）
	private String keyword1;	// 關鍵字1		（UI & Batch）
	private String keyword2;	// 關鍵字2		（UI & Batch）
	private String keyword3;	// 關鍵字3		（UI & Batch）
	private String message;		// 事 & 物		（UI & Batch & TPIPAS）
	private String dataCount;	// 資料筆數		（TPIPAS）

	public LogEntry(JSONObject json) {
	}

	public List<Object> deserialize(byte[] bytes) {
		return null;
	}

	public Fields getOutputFields() {
		return null;
	}

}
