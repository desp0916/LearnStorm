package com.pic.ala.gen;

final public class ImmutableApLog {

	private final String sysID;		// 系統名稱 (new)
	private final String logType;	// Log 類型 (new)
	private final String logTime;	// Log 寫入時間，必須符合「yyyy-mm-dd hh:mm:ss.sss」格式

	private final String apID;		// AP 名稱，可帶版本
	private final String functID;	// 功能代碼或原始碼中的 class、method

	private final String who;		// 誰發起這個 request 或觸發這個 event，例如： User ID

	private final String from; 		// 請求的來源，例如：client ip
	private final String at;		// 處理請求的地方，例如：本機的 host name
	private final String to;		// 目的地，例：DB 的 host name、遠端的 Web Service / API

	private final String action;	// 執行動作
	private final String result;	// 執行結果

	private final String kw;		// 關鍵字

	private final String msgLevel;	// 訊息層級
	private final String msg;		// 訊息內容
	private final String msgCode;	// 訊息代碼

	private final String table;		// 資料表名稱 (optional)
	private final int dataCnt;		// 資料筆數 (optional)

	public ImmutableApLog() {
		this.sysID = "";
		this.logType = "";
		this.logTime = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
		this.sysID = "";
	}

	public ImmutableApLog(String sysID, String logType, String logTime, String apID, String functID, String who,
			String from, String at, String to, String action, String result, String kw, String msgLevel, String msg,
			String msgCode, String table, int dataCnt) {
		this.sysID = sysID;
		this.logType = logType;
		this.logTime = logTime;
		this.apID = apID;
		this.functID = functID;
		this.who = who;
		this.from = from;
		this.at = at;
		this.to = to;
		this.action = action;
		this.result = result;
		this.kw = kw;
		this.msgLevel = msgLevel;
		this.msg = msg;
		this.msgCode = msgCode;
		this.table = table;
		this.dataCnt = dataCnt;
	}

	public String getSysID() {
		return sysID;
	}

	public String getLogType() {
		return logType;
	}

	public String getLogTime() {
		return logTime;
	}

	public String getApID() {
		return apID;
	}

	public String getFunctID() {
		return functID;
	}

	public String getWho() {
		return who;
	}

	public String getFrom() {
		return from;
	}

	public String getAt() {
		return at;
	}

	public String getTo() {
		return to;
	}

	public String getAction() {
		return action;
	}

	public String getResult() {
		return result;
	}

	public String getKw() {
		return kw;
	}

	public String getMsgLevel() {
		return msgLevel;
	}

	public String getMsg() {
		return msg;
	}

	public String getMsgCode() {
		return msgCode;
	}

	public String getTable() {
		return table;
	}

	public int getDataCnt() {
		return dataCnt;
	}

}
