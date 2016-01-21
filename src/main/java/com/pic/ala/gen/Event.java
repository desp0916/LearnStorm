package com.pic.ala.gen;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class Event {

	protected ObjectMapper mapper;
	protected String sysID; // 系統 ID
	protected Logger logger;

	public String getSysID() {
		return sysID;
	}
}
