package com.pic.ala.gen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public class TPIPASEvent {

	private String systemID; 	// 系統 ID
	private int duration; 		// 執行時間

	public TPIPASEvent(String systemID) {
		this.systemID = systemID;
		// 1~20 秒之間
		this.duration = ThreadLocalRandom.current().nextInt(1, 21);
	}

	public void fire() {
		try {
	        long threadId = Thread.currentThread().getId();
			Thread.sleep(duration * 1000);
	        Calendar cal = Calendar.getInstance();
	        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	        System.out.println(systemID + "\tTPIPAS\t" + threadId + "\t" + sdf.format(cal.getTime()));
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}