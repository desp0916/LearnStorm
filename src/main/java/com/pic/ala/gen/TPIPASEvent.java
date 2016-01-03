package com.pic.ala.gen;

import java.util.concurrent.ThreadLocalRandom;

import com.pic.ala.log.Log;

public class TPIPASEvent {

	private volatile String systemID; // 系統 ID
	// private volatile int duration; // 執行時間

	public TPIPASEvent(String systemID) {
		this.systemID = systemID;
	}

	public String getSystemId() {
		return systemID;
	}

	public void fire() {
		while (true) {
			try {
				synchronized (this) {
					wait(ThreadLocalRandom.current().nextInt(1, 21) * 1000);
//					System.out.println(systemID + "\tTPIPAS\t" + Thread.currentThread().getId() + "\t"
//							+ new Timestamp(new Date().getTime()));
					 Log log = new Log(systemID, "TPIPAS");
					 System.out.println(log.toString());
				}

			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}

	}
}