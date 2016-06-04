package com.pic.ala.model;

public class TPIPASEventThread extends Thread implements Command {

	private TPIPASEvent tpipasEvent;

	public TPIPASEventThread(final TPIPASEvent tpipasEvent) {
		this.tpipasEvent = tpipasEvent;
		this.setName(tpipasEvent.getSysID() + "-TPIPASEvent");
	}

	@Override
	public void run() {
		tpipasEvent.go();
	}

}
