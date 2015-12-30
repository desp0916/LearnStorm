package com.pic.ala.gen;

public class UIActionCommand extends Thread implements Command {

	private UIAction uiAction;

	public UIActionCommand(UIAction uiAction) {
		this.uiAction = uiAction;
	}

	@Override
	public void run() {
		uiAction.take();
	}

}
