package com.pic.ala.model;

/**
 * 採用 "Command" Design pattern
 *
 * https://openhome.cc/Gossip/DesignPattern/CommandPattern.htm
 *
 * @author gary
 * @since  2017年5月22日 下午2:53:51
 */
public class Command extends Thread {

	private Event event;

	public Command(Event event) {
		this.event = event;
		this.setName(event.getSysID() + "-" + event.getClass().getSimpleName());
	}

	@Override
	public void run() {
		this.event.go();
	}
}