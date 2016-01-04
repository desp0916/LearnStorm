package com.pic.ala.gen;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

public class BatchJob {

	private volatile String systemID; // 系統 ID
	// private volatile int duration; // 執行時間
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
//				System.out.println(systemID + "\tBATCH\t" + Thread.currentThread().getId() + "\t"
//						+ new Timestamp(new Date().getTime()) + "\tSTART");
				Log log = new Log(systemID, "BATCH");
//				System.out.println(log.toString());
				logger.info(log.toString());
				wait(ThreadLocalRandom.current().nextInt(1, 21) * 1000);
//				System.out.println(log.toString());
				logger.info(log.toString());
//				System.out.println(systemID + "\tBATCH\t" + Thread.currentThread().getId() + "\t"
//						+ new Timestamp(new Date().getTime()) + "\tEND");
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

	}

}