package com.pic.ala.gen;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Log {

	private static final String sep = "$$";
	private static final String[] systemIDs = new String[] { "AES", "POS", "UPCC", "SCP" };
	private static final String[] logTypes = new String[] { "UI", "BATCH", "TPIPAS" };

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
	private static List<String> messages = Arrays.asList("Wrong password.", "Lost connection.", "Invalid format", "Unsufficient privilege", "Disk full");
	private static List<String> messageCodes = Arrays.asList("10001", "23001", "12345", "56789", "23245");
	private static List<String> tableNames = Arrays.asList("SYS_USERS", "TRA_ORDERS", "TRA_INVOICES", "CODES", "ITEMS");
	private static List<String> dataCounts = Arrays.asList("1", "24", "100", "1234", "20344");

	private String systemID;		// 系統名稱 (new)
	private String logType;			// Log 類型 (new)
	private String logTime;			// Log 寫入時間，必須符合「yyyy-mm-dd hh:mm:ss.sss」格式

	private String apName = "APv2";	// AP 名稱，可帶版本
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

	public Log(String systemID, String logType) {

		this.systemID = systemID;
		this.logType = logType;
		this.logTime = new Timestamp(new Date().getTime()).toString();

		if (("UI").equals(logType)) {
			this.from = getOption(webServers);
			this.at = getOption(apServers);
			this.to = getOption(dbServers);
			this.who = getOption(users);
			this.action = getOption(actions);
		} else if (("BATCH").equals(logType)) {
			this.from = getOption(apServers);
			this.at = getOption(batchServers);
			this.to = getOption(dbServers);
			this.who = getOption(users);
			this.action = getOption(actions);
		} else if (("TPIPAS").equals(logType)) {
			getAllServers();
			this.from = getOption(allServers);
			this.at = getOption(allServers);
			this.to = getOption(allServers);
			this.who = getOption(users);
			this.action = getOption(actions);
		}

		this.result = getOption(results);
		this.keyword = getOption(keywords);
		this.messageLevel = getOption(messageLevels);
		this.message = getOption(messages);
		this.messageCode = getOption(messageCodes);
		this.tableName = getOption(tableNames);
		this.dataCount = getOption(dataCounts);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(systemID).append(sep)
				.append(logType).append(sep)
				.append(logTime).append(sep)
				.append(apName).append(sep)
				.append(functionID).append(sep)
				.append(who).append(sep)
				.append(from).append(sep)
				.append(at).append(sep)
				.append(to).append(sep)
				.append(action).append(sep)
				.append(result).append(sep)
				.append(keyword).append(sep)
				.append(messageLevel).append(sep)
				.append(message).append(sep)
				.append(messageCode).append(sep)
				.append(tableName).append(sep)
				.append(dataCount);

		return builder.toString();
	}

	private void getAllServers() {
		allServers.addAll(webServers);
		allServers.addAll(apServers);
		allServers.addAll(batchServers);
		allServers.addAll(dbServers);
	}

	public String getKeyword() {
		return this.keyword;
	}


	private static String getOption(List<String> options) {
		Random rand = new Random();
		return options.get(rand.nextInt(options.size()));
	}

	public static void main(String[] args) {
		Log log = new Log("AES", "UI");
//		System.out.println(log.getKeyword());
	}
}