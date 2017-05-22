/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/1.7/mapping-date-format.html#built-in-date-formats
 * http://stackoverflow.com/questions/4486787/jackson-with-json-unrecognized-field-not-marked-as-ignorable
 */
package com.pic.ala.model;

import static com.pic.ala.util.LogUtil.getISO8601Time;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * AP Log Entity
 *
 * @author gary
 * @since  2017年5月22日 下午3:00:56
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApLog {

	public static final List<String> SYSTEMS;
	public static List<String> LOG_TYPES;

	public static final String LOG_SEPARATOR = "$$";
	public static final String DEFAULT_SYS_ID = "aes3g";
	public static final String DEFAULT_LOG_FORMAT = "all";
	public static final String DEFAULT_LOG_TYPE = "batch";

	private static final List<String> apIDs, functionIDs, users, allServers, webServers,
		internetIPs, apServers, batchServers, dbServers, actions, results, keywords, messageLevels, messages,
		tableNames;

	static {
		// Index names of Elasticsearch should be lower cases.
		SYSTEMS = Arrays.asList(DEFAULT_SYS_ID, "pos", "upcc", "wds");
		LOG_TYPES = Arrays.asList(DEFAULT_LOG_TYPE, ApLogType.UI.getValue(), ApLogType.TPIPAS.getValue(), ApLogType.API.getValue());
		apIDs = Arrays.asList("App01V4", "App02V2", "App03V4", "App04V3", "App05V1");
		functionIDs = Arrays.asList("訂單明細", "折讓單", "發票", "出貨單", "托運單", "訂單", "進貨單");
		users = Arrays.asList("總經理", "副總", "部長", "經理", "專員A", "專員B", "廠商A", "廠商B", "消費者A", "消費者B");
		allServers = new ArrayList<String>();
		webServers = Arrays.asList("apache", "iis", "nginx", "proxy");
		internetIPs = Arrays.asList("61.57.231.227", "114.136.21.238", "8.8.8.8", "168.95.1.1");
		apServers = Arrays.asList("tomcat", "jboss", "iis", "websphere");
		batchServers = Arrays.asList("batch01", "batch02", "batch03", "batch04");
		dbServers = Arrays.asList("edb", "mssql", "mysql", "oracle", "postgres");
		actions = Arrays.asList("add", "delete", "query", "edit");
		results = Arrays.asList("成功", "失敗", "放棄", "取消", "逾時");
		keywords = Arrays.asList("原力覺醒", "史努比", "玩命關頭", "侏儸紀世界", "怪物遊戲", "追殺比爾", "SuperMan", "AntMan", "速達3G", "andy.li");
		messageLevels = Arrays.asList("FATAL", "ERROR", "WARNING", "NOTICE", "INFO", "DEBUG");
		messages = Arrays.asList("成功", "速達3G", "OK", "OK", "Wrong password.", "Lost connection.", "Invalid arguments", "Unsufficient privilege", "Disk full", genStackTrace());
		tableNames = Arrays.asList("SYS_USERS", "TRA_ORDERS", "TRA_INVOICES", "CODES", "ITEMS");
	}

//	private static List<String> messageCodes = Arrays.asList("10001", "23001", "12345", "56789", "23245");
//	private static List<String> dataCounts = Arrays.asList("1", "24", "100", "1234", "20344");

	private String sysID;				// 系統名稱 (new)
	private String logType;				// Log 類型 (new)
	private String logTime;				// Log 寫入時間，必須符合「yyyy-mm-dd hh:mm:ss.sss」格式

	private String apID = "";			// AP 名稱，可帶版本
	private String functID = "";		// 功能代碼或原始碼中的 class、method

	private String who = "";			// 誰發起這個 request 或觸發這個 event，例如： User ID

	private String reqFrom = ""; 		// 請求的來源，例如：client ip
	private String reqAt = "";			// 處理請求的地方，例如：本機的 host name
	private String reqTo = "";			// 目的地，例：DB 的 host name、遠端的 Web Service / API

	private String reqAction = "";		// 執行動作
	private String reqResult = "";		// 執行結果

	private String kw = "";				// 關鍵字

	private String msgLevel = "INFO";	// 訊息層級 (optional)
	private String msg = "";			// 訊息內容
	private String msgCode = "";		// 訊息代碼 (optional)

	private String reqTable = "";		// 資料表名稱 (optional)
	private int dataCnt;				// 資料筆數 (optional)
	private Integer procTime;			// 處理時間（optional）

	public ApLog() {}

	public ApLog(final String systemID, final String logType) {
		this(systemID, logType, null);
	}

	public ApLog(final String systemID, final String logType, final String outputFields) {

		if (systemID != null && !("").equals(systemID) && SYSTEMS.contains(systemID)) {
			this.sysID = systemID.toLowerCase();
		} else {
			this.sysID = DEFAULT_SYS_ID.toLowerCase();
		}

		if (logType == null || ("").equals(logType) || !LOG_TYPES.contains(logType)) {
			this.logType = DEFAULT_LOG_TYPE;
		} else {
			this.logType = logType;
		}

		if (logType == ApLogType.UI.getValue()) {
			this.apID = logType.toUpperCase() +  getRandomOption(apIDs);
			this.reqFrom = getRandomOption(internetIPs);
			this.reqAt = getRandomOption(apServers);
			this.reqTo = getRandomOption(dbServers);
			this.who = getRandomOption(users);
			this.reqAction = getRandomOption(actions);
		} else if (logType == ApLogType.TPIPAS.getValue()) {
			getAllServers();
			this.apID = logType.toUpperCase() +  getRandomOption(apIDs);
			this.reqFrom = getRandomOption(internetIPs);
			this.reqAt = getRandomOption(allServers);
			this.reqTo = getRandomOption(allServers);
			this.who = getRandomOption(users);
			this.reqAction = getRandomOption(actions);
		} else if (logType == ApLogType.BATCH.getValue() || logType == ApLogType.API.getValue()) {
			this.apID = logType.toUpperCase() +  getRandomOption(apIDs);
			this.reqFrom = getRandomOption(internetIPs);
			this.reqAt = getRandomOption(batchServers);
			this.reqTo = getRandomOption(dbServers);
			this.who = getRandomOption(users);
			this.reqAction = getRandomOption(actions);
		}

		this.logTime = getISO8601Time();
		this.functID = getRandomOption(functionIDs);
		this.reqResult = getRandomOption(results);
		this.kw = getRandomOption(keywords);
		this.msgLevel = getRandomOption(messageLevels);
		this.msg = getRandomOption(messages);
		this.msgCode = String.valueOf(getRandomInt(1000, 9999));
		this.reqTable = getRandomOption(tableNames);
		this.dataCnt = getRandomInt(1, 200);
		this.procTime = getRandomInt(1, 200);

		// 如果只想產生部分欄位的話...
		if ("partial".equals(outputFields)) {
			this.functID = null;
			this.kw = null;
			this.msgLevel = null;
			this.msgCode = null;
			this.reqTable = null;
			this.logTime = getISO8601Time("yyyy-MM-dd HH:mm:ss.SSS");
			this.procTime = null;
			LOG_TYPES = Arrays.asList("tpipas");
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(sysID).append(LOG_SEPARATOR)
				.append(logType).append(LOG_SEPARATOR)
				.append(logTime).append(LOG_SEPARATOR)
				.append(apID).append(LOG_SEPARATOR)
				.append(functID).append(LOG_SEPARATOR)
				.append(who).append(LOG_SEPARATOR)
				.append(reqFrom).append(LOG_SEPARATOR)
				.append(reqAt).append(LOG_SEPARATOR)
				.append(reqTo).append(LOG_SEPARATOR)
				.append(reqAction).append(LOG_SEPARATOR)
				.append(reqResult).append(LOG_SEPARATOR)
				.append(kw).append(LOG_SEPARATOR)
				.append(msgLevel).append(LOG_SEPARATOR)
				.append(msg).append(LOG_SEPARATOR)
				.append(msgCode).append(LOG_SEPARATOR)
				.append(reqTable).append(LOG_SEPARATOR)
				.append(dataCnt).append(LOG_SEPARATOR)
				.append(procTime);

		return builder.toString();
	}

	public String getSysID() {
		return sysID;
	}

	public void setSysID(String sysID) {
		this.sysID = sysID;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}

	public String getApID() {
		return apID;
	}

	public void setApID(String apID) {
		this.apID = apID;
	}

	public String getFunctID() {
		return functID;
	}

	public void setFunctID(String functID) {
		this.functID = functID;
	}

	public String getWho() {
		return who;
	}

	public void setWho(String who) {
		this.who = who;
	}

	public String getReqFrom() {
		return reqFrom;
	}

	public void setReqFrom(String reqFrom) {
		this.reqFrom = reqFrom;
	}

	public String getReqAt() {
		return reqAt;
	}

	public void setReqAt(String reqAt) {
		this.reqAt = reqAt;
	}

	public String getReqTo() {
		return reqTo;
	}

	public void setReqTo(String reqTo) {
		this.reqTo = reqTo;
	}

	public String getReqAction() {
		return reqAction;
	}

	public void setReqAction(String reqAction) {
		this.reqAction = reqAction;
	}

	public String getReqResult() {
		return reqResult;
	}

	public void setResult(String result) {
		this.reqResult = result;
	}

	public String getKw() {
		return kw;
	}

	public void setKw(String kw) {
		this.kw = kw;
	}

	public String getMsgLevel() {
		return msgLevel;
	}

	public void setMsgLevel(String msgLevel) {
		this.msgLevel = msgLevel;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsgCode() {
		return msgCode;
	}

	public void setMsgCode(String msgCode) {
		this.msgCode = msgCode;
	}

	public String getReqTable() {
		return reqTable;
	}

	public void setReqTable(String reqTable) {
		this.reqTable = reqTable;
	}

	public int getDataCnt() {
		return dataCnt;
	}

	public void setDataCnt(int dataCnt) {
		this.dataCnt = dataCnt;
	}

	public Integer getProcTime() {
		return procTime;
	}

	public void setProcTime(Integer procTime) {
		this.procTime = procTime;
	}

	/**
	 * Merge and return all servers.
	 */
	private void getAllServers() {
		allServers.addAll(webServers);
		allServers.addAll(apServers);
		allServers.addAll(batchServers);
		allServers.addAll(dbServers);
	}

	public static String getRandomOption(final List<String> options) {
		Random rand = new Random();
		return options.get(rand.nextInt(options.size()));
	}

	public static int getRandomInt(final int minInt, final int maxInt) {
		return ThreadLocalRandom.current().nextInt(minInt, maxInt + 1);
	}

	public static void main(String[] args) {
		ApLog log = new ApLog(DEFAULT_SYS_ID, DEFAULT_LOG_FORMAT, DEFAULT_LOG_TYPE);
		System.out.println(log.toString());
	}

	private static String genStackTrace() {
		StringWriter errors = new StringWriter();
		try {
			int i = 1 / 0;
		} catch (Exception ex) {
			ex.printStackTrace(new PrintWriter(errors));
		}
		return errors.toString();
	}
}