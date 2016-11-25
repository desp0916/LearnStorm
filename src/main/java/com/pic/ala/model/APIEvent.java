package com.pic.ala.model;

public class APIEvent extends Event {

	public APIEvent(final String sysID) {
		super(sysID, ApLogType.API.getValue(), APIEvent.class);
	}

}
