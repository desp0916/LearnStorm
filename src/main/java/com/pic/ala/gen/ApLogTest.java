/**
 * AP Log 產生器（於 local 端執行，使用 log4j 的「Kafka appender」寫入 Kafka ）
 */
package com.pic.ala.gen;

public class ApLogTest {

	public static void main(String[] args) {
		startAllThreads();
	}

	public static void startAllThreads() {
		boolean enableSleep = false;
		for (String sysID : ApLog.SYSTEMS) {
			(new BatchJobThread(new BatchJob(sysID, enableSleep))).start();
			(new UIActionThread(new UIAction(sysID, enableSleep))).start();
			(new TPIPASEventThread(new TPIPASEvent(sysID, enableSleep))).start();
		}
	}

}