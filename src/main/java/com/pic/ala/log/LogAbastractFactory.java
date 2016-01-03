package com.pic.ala.log;

public abstract class LogAbastractFactory {

	protected abstract Log createLog(String systemID, String logType);

	public Log getLog(String systemID, String logType) {
		Log log = createLog(systemID, logType);
		return log;
	}

}