package com.pic.ala.gen;

public class TPIPASEventCommand extends Thread implements Command {

	private TPIPASEvent tpipasEvent;

	public TPIPASEventCommand(TPIPASEvent tpipasEvent) {
		this.tpipasEvent = tpipasEvent;
		this.setName(tpipasEvent.getSystemId() + "-TPIPASEvent");
	}

	@Override
	public void run() {
		tpipasEvent.fire();
	}

}
