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

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApLog {

	public static final String LOG_SEPARATOR = "$$";

	public static final String DEFAULT_SYS_ID = "aes3g";
	public static final String DEFAULT_LOG_TYPE = "batch";

	// Index names of Elasticsearch should be lower cases.
	public static final List<String> SYSTEMS = Arrays.asList(DEFAULT_SYS_ID, "pos", "upcc", "wds");
	public static final List<String> LOG_TYPES = Arrays.asList(DEFAULT_LOG_TYPE, "ui", "tpipas", "api");

	private static List<String> apIDs = Arrays.asList("App01V4", "App02V2", "App03V4", "App04V3", "App05V1");
	private static List<String> functionIDs = Arrays.asList("訂單明細", "折讓單", "發票", "出貨單", "托運單", "訂單", "進貨單");
	private static List<String> users = Arrays.asList("總經理", "副總", "部長", "經理", "專員A", "專員B", "廠商A", "廠商B", "消費者A", "消費者B");
	private static List<String> allServers = new ArrayList<String>();
	private static List<String> webServers = Arrays.asList("apache", "iis", "nginx", "proxy");
	private static List<String> internetIPs = Arrays.asList("61.57.231.227", "114.136.21.238", "8.8.8.8", "168.95.1.1");
	private static List<String> apServers = Arrays.asList("tomcat", "jboss", "iis", "websphere");
	private static List<String> batchServers = Arrays.asList("batch01", "batch02", "batch03", "batch04");

	private static List<String> dbServers = Arrays.asList("edb", "mssql", "mysql", "oracle", "postgres");

	private static List<String> actions = Arrays.asList("add", "delete", "query", "edit");
	private static List<String> results = Arrays.asList("成功", "失敗", "放棄", "取消", "逾時");
	private static List<String> keywords = Arrays.asList("原力覺醒", "史努比", "玩命關頭", "侏儸紀世界", "怪物遊戲");
	private static List<String> messageLevels = Arrays.asList("FATAL", "ERROR", "WARNING", "NOTICE", "INFO", "DEBUG");
	private static List<String> messages = Arrays.asList("OK", "OK", "OK", "Wrong password.", "Lost connection.", "Invalid arguments", "Unsufficient privilege", "Disk full", genStackTrace());
//	private static List<String> messageCodes = Arrays.asList("10001", "23001", "12345", "56789", "23245");
	private static List<String> tableNames = Arrays.asList("SYS_USERS", "TRA_ORDERS", "TRA_INVOICES", "CODES", "ITEMS");
//	private static List<String> dataCounts = Arrays.asList("1", "24", "100", "1234", "20344");

	private String sysID;				// 系統名稱 (new)
	private String logType;				// Log 類型 (new)
	private String logTime;				// Log 寫入時間，必須符合「yyyy-mm-dd hh:mm:ss.sss」格式

	private String apID = "";			// AP 名稱，可帶版本
	private String functID = "";		// 功能代碼或原始碼中的 class、method

	private String who = "";			// 誰發起這個 request 或觸發這個 event，例如： User ID

	private String from = ""; 			// 請求的來源，例如：client ip
	private String at = "";				// 處理請求的地方，例如：本機的 host name
	private String to = "";				// 目的地，例：DB 的 host name、遠端的 Web Service / API

	private String action = "";			// 執行動作
	private String result = "";			// 執行結果

	private String kw = "";				// 關鍵字

	private String msgLevel = "INFO";	// 訊息層級 (optional)
	private String msg = "";			// 訊息內容
	private String msgCode = "";		// 訊息代碼 (optional)

	private String table = "";			// 資料表名稱 (optional)
	private int dataCnt;				// 資料筆數 (optional)
	private int procTime;				// 處理時間（optional）

	public ApLog() {}

	public ApLog(final String systemID, final String logType) {

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

		if (logType == "ui") {
			this.apID = logType.toUpperCase() +  getRandomOption(apIDs);
			this.from = getRandomOption(internetIPs);
			this.at = getRandomOption(apServers);
			this.to = getRandomOption(dbServers);
			this.who = getRandomOption(users);
			this.action = getRandomOption(actions);
		} else if (logType == "tpipas") {
			getAllServers();
			this.apID = logType.toUpperCase() +  getRandomOption(apIDs);
			this.from = getRandomOption(internetIPs);
			this.at = getRandomOption(allServers);
			this.to = getRandomOption(allServers);
			this.who = getRandomOption(users);
			this.action = getRandomOption(actions);
		} else if (logType == "batch" || logType == "api") {
			this.apID = logType.toUpperCase() +  getRandomOption(apIDs);
			this.from = getRandomOption(internetIPs);
			this.at = getRandomOption(batchServers);
			this.to = getRandomOption(dbServers);
			this.who = getRandomOption(users);
			this.action = getRandomOption(actions);
		}

		this.logTime = getISO8601Time();
		this.functID = getRandomOption(functionIDs);
		this.result = getRandomOption(results);
		this.kw = getRandomOption(keywords);
		this.msgLevel = getRandomOption(messageLevels);
		this.msg = getRandomOption(messages);
		this.msgCode = String.valueOf(getRandomInt(1000, 9999));
		this.table = getRandomOption(tableNames);
		this.dataCnt = getRandomInt(1, 200);
		this.procTime = getRandomInt(1, 200);
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
				.append(from).append(LOG_SEPARATOR)
				.append(at).append(LOG_SEPARATOR)
				.append(to).append(LOG_SEPARATOR)
				.append(action).append(LOG_SEPARATOR)
				.append(result).append(LOG_SEPARATOR)
				.append(kw).append(LOG_SEPARATOR)
				.append(msgLevel).append(LOG_SEPARATOR)
				.append(msg).append(LOG_SEPARATOR)
				.append(msgCode).append(LOG_SEPARATOR)
				.append(table).append(LOG_SEPARATOR)
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

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getAt() {
		return at;
	}

	public void setAt(String at) {
		this.at = at;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
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

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public int getDataCnt() {
		return dataCnt;
	}

	public void setDataCnt(int dataCnt) {
		this.dataCnt = dataCnt;
	}

	public int getProcTime() {
		return procTime;
	}

	public void setProcTime(int procTime) {
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
		ApLog log = new ApLog(DEFAULT_SYS_ID, DEFAULT_LOG_TYPE);
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