/**
 * AP Log 產生器（於 local 端執行，使用 log4j 的「Kafka appender」寫入 Kafka ）
 */
package com.pic.ala.gen;

public class ApLogTest {

	public static void main(String[] args) {
		startAllThreads();
	}

	public static void startAllThreads() {
		for (String systemID : ApLog.SYSTEMS) {
			new BatchJobThread(new BatchJob(systemID)).start();
			new UIActionThread(new UIAction(systemID)).start();
			new TPIPASEventThread(new TPIPASEvent(systemID)).start();
		}
	}

}