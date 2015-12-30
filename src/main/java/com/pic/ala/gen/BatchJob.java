package com.pic.ala.gen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public class BatchJob {

	private String systemID;	// 系統 ID
	private int duration;		// 執行時間

	public BatchJob(String systemID) {
		this.systemID = systemID;
		// 1~20 秒之間
		this.duration = ThreadLocalRandom.current().nextInt(1, 21);
	}

	public void run() {
		try {
	        long threadId = Thread.currentThread().getId();
	        Calendar cal = Calendar.getInstance();
	        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	        System.out.println(systemID + "\tBATCH\t" + threadId + "\t" + sdf.format(cal.getTime()) + "\tSTART");
			Thread.sleep(duration * 1000);
			System.out.println(systemID + "\tBATCH\t" + threadId + "\t" + sdf.format(cal.getTime()) + "\tEND");
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

}