package com.pic.ala.gen;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

public class BatchJob {

	private volatile String systemID; // 系統 ID
	private static Logger logger = Logger.getLogger(BatchJob.class);

	public BatchJob(String systemID) {
		this.systemID = systemID;
	}

	public String getSystemId() {
		return systemID;
	}

	public void run() {
		while (true) {
			try {
				synchronized (this) {
					ApLog log = new ApLog(systemID, "BATCH");
					logger.info(log.toString());
					wait(ThreadLocalRandom.current().nextInt(1, 21) * 1000);
					logger.info(log.toString());
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

	}

}