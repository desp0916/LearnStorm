package com.pic.ala.gen;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class BatchJob {

	private volatile String systemID; 	// 系統 ID
	private volatile int duration; 		// 執行時間

	public BatchJob(String systemID) {
		this.systemID = systemID;
	}

	public String getSystemId() {
		return systemID;
	}

	public void run() {
		try {
			while (true) {
				System.out.println(systemID + "\tBATCH\t"
						+ Thread.currentThread().getId() + "\t"
						+ new Timestamp(new Date().getTime()) + "\tSTART");
				duration = ThreadLocalRandom.current().nextInt(1, 21);
				Thread.sleep(duration * 1000);
				System.out.println(systemID + "\tBATCH\t"
						+ Thread.currentThread().getId() + "\t"
						+ new Timestamp(new Date().getTime()) + "\tEND");
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

}