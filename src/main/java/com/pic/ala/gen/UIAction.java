package com.pic.ala.gen;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

public class UIAction {

	private volatile String systemID; // 系統 ID
	private static Logger logger = Logger.getLogger(UIAction.class);

	public UIAction(String systemID) {
		this.systemID = systemID;
	}

	public String getSystemId() {
		return systemID;
	}

	public void take() {
		while (true) {
			try {
				synchronized (this) {
					wait(ThreadLocalRandom.current().nextInt(1, 121) * 1000);
					ApLog log = new ApLog(systemID, "ui");
					logger.info(log.toString());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}