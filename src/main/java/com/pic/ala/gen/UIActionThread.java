package com.pic.ala.gen;

public class UIActionThread extends Thread implements Command {

	private UIAction uiAction;

	public UIActionThread(UIAction uiAction) {
		this.uiAction = uiAction;
		this.setName(uiAction.getSystemId() + "-UIAction");
	}

	@Override
	public void run() {
		uiAction.take();
	}

}
