package com.pic.ala.model;

public class UIActionThread extends Thread implements Command {

	private UIAction uiAction;

	public UIActionThread(final UIAction uiAction) {
		this.uiAction = uiAction;
		this.setName(uiAction.getSysID() + "-UIAction");
	}

	@Override
	public void run() {
		uiAction.take();
	}

}
