package com.pic.ala.gen;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class TPIPASEvent {

	private volatile String systemID; 	// 系統 ID
	private volatile int duration; 		// 執行時間

	public TPIPASEvent(String systemID) {
		this.systemID = systemID;
	}

	public String getSystemId() {
		return systemID;
	}
	public void fire() {
		try {
			while (true) {
				duration = ThreadLocalRandom.current().nextInt(1, 21);
				Thread.sleep(duration * 1000);
				System.out.println(systemID + "\tTPIPAS\t"
						+ Thread.currentThread().getId() + "\t"
						+ new Timestamp(new Date().getTime()));
			}
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}