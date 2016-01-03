package com.pic.ala.gen;

import java.util.Random;

public class LogTest {

	public static void main(String[] args) {
		startAllCommands();
	}

	public static void startAllCommands() {
//		String[] systems = new String[] {"AES", "POS", "UPCC", "SCP"};
		String[] systems = new String[] {"AES", "POS"};
		for (int i = 0; i < systems.length; i++) {
			new BatchJobThread(new BatchJob(systems[i])).start();
			new UIActionThread(new UIAction(systems[i])).start();
			new TPIPASEventThread(new TPIPASEvent(systems[i])).start();
		}
	}

	private static Command getCommand() {

		BatchJob batchJob = new BatchJob(getSystem());
		UIAction uiJob = new UIAction(getSystem());
		TPIPASEvent tpipasEvent = new TPIPASEvent(getSystem());

		BatchJobThread bjc = new BatchJobThread(batchJob);
		UIActionThread uac = new UIActionThread(uiJob);
		TPIPASEventThread tec = new TPIPASEventThread(tpipasEvent);

		Command[] commands = new Command[] {bjc, uac, tec};

		return commands[new Random().nextInt(commands.length)];
	}

	private static String getSystem() {
		Random rand = new Random();
		String[] systems = new String[] {"AES", "POS", "UPCC", "SCP"};
		return systems[rand.nextInt(systems.length)];
	}

}
