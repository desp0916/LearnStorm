package com.pic.ala.gen;

public class TPIPASEventCommand extends Thread implements Command {

	private TPIPASEvent tpipasEvent;

	public TPIPASEventCommand(TPIPASEvent tpipasEvent) {
		this.tpipasEvent = tpipasEvent;
	}

	@Override
	public void run() {
		tpipasEvent.fire();
	}

}
