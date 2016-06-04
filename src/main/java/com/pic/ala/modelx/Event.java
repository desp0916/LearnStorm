package com.pic.ala.model;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class Event {

	protected ObjectMapper mapper;
	protected String sysID; // 系統 ID
	protected Logger logger;
	protected boolean enableSleep;

	public String getSysID() {
		return sysID;
	}
}
