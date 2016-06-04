/**
 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
 */
package com.pic.ala.model;

public class UIAction extends Event {

	public UIAction(final String sysID) {
		super(sysID, ApLogType.UI.getValue(), UIAction.class);
	}

}