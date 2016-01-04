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