package com.pic.ala.gen;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ApLog {

	public static final String LOG_SEPARATOR = "$$";

	public static final String DEFAULT_SYSTEM = "aes3g";
	public static final String DEFAULT_LOG_TYPE = "batch";

	// Index names of Elasticsearch should be lower cases.
	public static final List<String> SYSTEMS = Arrays.asList(DEFAULT_SYSTEM, "pos", "upcc", "wds");
	public static final List<String> LOG_TYPES = Arrays.asList(DEFAULT_LOG_TYPE, "ui", "tpipas");

	private static List<String> apNames = Arrays.asList("APv1", "WebAppV1", "WebServiceV2", "RESTAPIv1", "TransBatchv1");
	private static List<String> functionIDs = Arrays.asList("FUNC10000", "FUNC10001", "FUNC10002", "FUNC10002", "FUNC10003");
	private static List<String> users = Arrays.asList("艾莉絲", "班潔明", "王小明", "丹尼爾");
	private static List<String> allServers = new ArrayList<String>();
	private static List<String> webServers = Arrays.asList("apache", "iis", "nginx", "proxy");
	private static List<String> apServers = Arrays.asList("tomcat", "jboss", "iis", "websphere");
	private static List<String> batchServers = Arrays.asList("batch01", "batch02", "batch03", "batch04");
	private static List<String> dbServers = Arrays.asList("edb", "mssql", "mysql", "oracle", "postgres");

	private static List<String> actions = Arrays.asList("登入", "登出", "註冊", "訂單成立", "放入購物車");
	private static List<String> results = Arrays.asList("成功", "失敗", "放棄", "取消", "逾時");
	private static List<String> keywords = Arrays.asList("原力覺醒", "史努比", "玩命關頭", "侏羅紀世界", "怪物遊戲");
	private static List<String> messageLevels = Arrays.asList("FATAL", "ERROR", "WARNING", "NOTICE", "INFO", "DEBUG");
	private static List<String> messages = Arrays.asList("Wrong password.", "Lost connection.", "Invalid arguments", "Unsufficient privilege", "Disk full");
	private static List<String> messageCodes = Arrays.asList("10001", "23001", "12345", "56789", "23245");
	private static List<String> tableNames = Arrays.asList("SYS_USERS", "TRA_ORDERS", "TRA_INVOICES", "CODES", "ITEMS");
	private static List<String> dataCounts = Arrays.asList("1", "24", "100", "1234", "20344");

	private String systemID;		// 系統名稱 (new)
	private String logType;			// Log 類型 (new)
	private String logTime;			// Log 寫入時間，必須符合「yyyy-mm-dd hh:mm:ss.sss」格式

	private String apName = "APv1";	// AP 名稱，可帶版本
	private String functionID = "";	// 功能代碼或原始碼中的 class、method

	private String who = "";		// 誰發起這個 request 或觸發這個 event，例如： User ID

	private String from = ""; 		// 請求的來源，例如：client ip
	private String at = "";			// 處理請求的地方，例如：本機的 host name
	private String to = "";			// 目的地，例：DB 的 host name、遠端的 Web Service / API

	private String action = "";		// 執行動作
	private String result = "";		// 執行結果

	private String keyword = "";	// 關鍵字

	private String messageLevel = "INFO";	// 訊息層級
	private String message = "";			// 訊息內容
	private String messageCode = "";		// 訊息代碼

	private String tableName = "";	// 資料表名稱 (optional)
	private String dataCount = "";	// 資料筆數 (optional)

	public ApLog(String systemID, String logType) {

		if (systemID != null && !("").equals(systemID) && SYSTEMS.contains(systemID)) {
			this.systemID = systemID.toLowerCase();
		} else {
			this.systemID = DEFAULT_SYSTEM.toLowerCase();
		}

//		System.out.println("logType:"+logType);

		if (logType == null || ("").equals(logType) || !LOG_TYPES.contains(logType)) {
			this.logType = DEFAULT_LOG_TYPE;
		} else {
			this.logType = logType;
		}

		if (logType == "ui") {
			this.apName = "WebAppv1";
			this.from = getRandomOption(webServers);
			this.at = getRandomOption(apServers);
			this.to = getRandomOption(dbServers);
			this.who = getRandomOption(users);
			this.action = getRandomOption(actions);
		} else if (logType == "tpipas") {
			getAllServers();
			this.apName = getRandomOption(apNames);
			this.from = getRandomOption(allServers);
			this.at = getRandomOption(allServers);
			this.to = getRandomOption(allServers);
			this.who = getRandomOption(users);
			this.action = getRandomOption(actions);
		} else if (logType == "batch") {
			this.apName = "TransBatchv4";
			this.from = getRandomOption(apServers);
			this.at = getRandomOption(batchServers);
			this.to = getRandomOption(dbServers);
			this.who = getRandomOption(users);
			this.action = getRandomOption(actions);
		}

		this.logTime = new Timestamp(new Date().getTime()).toString();
		this.functionID = getRandomOption(functionIDs);
		this.result = getRandomOption(results);
		this.keyword = getRandomOption(keywords);
		this.messageLevel = getRandomOption(messageLevels);
		this.message = getRandomOption(messages);
		this.messageCode = getRandomOption(messageCodes);
		this.tableName = getRandomOption(tableNames);
		this.dataCount = getRandomOption(dataCounts);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(systemID).append(LOG_SEPARATOR)
				.append(logType).append(LOG_SEPARATOR)
				.append(logTime).append(LOG_SEPARATOR)
				.append(apName).append(LOG_SEPARATOR)
				.append(functionID).append(LOG_SEPARATOR)
				.append(who).append(LOG_SEPARATOR)
				.append(from).append(LOG_SEPARATOR)
				.append(at).append(LOG_SEPARATOR)
				.append(to).append(LOG_SEPARATOR)
				.append(action).append(LOG_SEPARATOR)
				.append(result).append(LOG_SEPARATOR)
				.append(keyword).append(LOG_SEPARATOR)
				.append(messageLevel).append(LOG_SEPARATOR)
				.append(message).append(LOG_SEPARATOR)
				.append(messageCode).append(LOG_SEPARATOR)
				.append(tableName).append(LOG_SEPARATOR)
				.append(dataCount);

		return builder.toString();
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

	public static String getRandomOption(List<String> options) {
		Random rand = new Random();
		return options.get(rand.nextInt(options.size()));
	}

	public static void main(String[] args) {
		ApLog log = new ApLog(DEFAULT_SYSTEM, DEFAULT_LOG_TYPE);
		System.out.println(log.toString());
	}
}