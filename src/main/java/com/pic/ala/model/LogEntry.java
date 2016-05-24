package com.pic.ala.model;

import static com.pic.ala.util.LogUtil.getISO8601Time;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public class LogEntry {

	public static final String FIELD_SEPARATOR = ",";

	public static final String DEFAULT_INDEX = "logstash";
	public static final String DEFAULT_TYPE = "unknown";

	private String _index;
	private String _type;
	private String timestamp;
	private String message;

	public LogEntry() {}

	public LogEntry(final String index, final String type) {
		if (index != null && !("").equals(index)) {
			this._index = index.toLowerCase();
		} else {
			this._index = DEFAULT_INDEX.toLowerCase();
		}

		if (type == null || ("").equals(type)) {
			this._type = DEFAULT_TYPE;
		} else {
			this._type = type;
		}
		this.timestamp = getISO8601Time();
		this.message = "";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("_index=").append(_index).append(FIELD_SEPARATOR)
				.append("_type=").append(_type).append(FIELD_SEPARATOR)
				.append("timestamp=").append(timestamp).append(FIELD_SEPARATOR)
				.append("message=").append(message).append(FIELD_SEPARATOR);

		return builder.toString();
	}

	public String getIndex() {
		return _index;
	}

	public void setIndex(String index) {
		this._index = index;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		this._type = type;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
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