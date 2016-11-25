package com.pic.ala.model;

public class APIEventThread extends Thread implements Command {

	private APIEvent apiEvent;

	public APIEventThread(final APIEvent apiEvent) {
		this.apiEvent = apiEvent;
		this.setName(apiEvent.getSysID() + "-APIEvent");
	}

	@Override
	public void run() {
		apiEvent.go();
	}

}
