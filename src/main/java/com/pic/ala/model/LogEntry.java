package com.pic.ala.model;

import static com.pic.ala.util.LogUtil.getISO8601Time;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public class LogEntry {

	public static final String FIELD_SEPARATOR = ",";

	public static final String DEFAULT_INDEX = "logstash";
	public static final String DEFAULT_TYPE = "unknown";

	private String index;
	private String type;
	private String logTime;
	private String host = "";
	private String message = "";

	public LogEntry() {}

	public LogEntry(final String index, final String type) {
		if (index != null && !("").equals(index)) {
			this.index = index.toLowerCase();
		} else {
			this.index = DEFAULT_INDEX.toLowerCase();
		}

		if (type == null || ("").equals(type)) {
			this.type = DEFAULT_TYPE;
		} else {
			this.type = type;
		}
		this.logTime = getISO8601Time();
		this.host = "";
		this.message = "";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("index=").append(index).append(FIELD_SEPARATOR)
				.append("type=").append(type).append(FIELD_SEPARATOR)
				.append("logTime=").append(logTime).append(FIELD_SEPARATOR)
				.append("host=").append(host).append(FIELD_SEPARATOR)
				.append("message=").append(message).append(FIELD_SEPARATOR);

		return builder.toString();
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLogTime() {
		return logTime;
	}

	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public static void main(String[] args) {
		LogEntry log = new LogEntry(DEFAULT_INDEX, DEFAULT_TYPE);
		System.out.println(log.toString());
	}

}