package com.pic.ala.gen;

public class TPIPASEventThread extends Thread implements Command {

	private TPIPASEvent tpipasEvent;

	public TPIPASEventThread(TPIPASEvent tpipasEvent) {
		this.tpipasEvent = tpipasEvent;
		this.setName(tpipasEvent.getSystemId() + "-TPIPASEvent");
	}

	@Override
	public void run() {
		tpipasEvent.fire();
	}

}
