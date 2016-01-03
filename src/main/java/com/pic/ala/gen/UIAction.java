package com.pic.ala.gen;

import java.util.concurrent.ThreadLocalRandom;

import com.pic.ala.log.Log;

public class UIAction {

	private volatile String systemID; // 系統 ID
	// private volatile int duration; // 執行時間

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
					wait(ThreadLocalRandom.current().nextInt(1, 21) * 1000);
//					System.out.println(systemID + "\tUI\t" + Thread.currentThread().getId() + "\t"
//							+ new Timestamp(new Date().getTime()));
					Log log = new Log(systemID, "UI");
					System.out.println(log.toString());
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}