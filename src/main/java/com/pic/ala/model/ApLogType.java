package com.pic.ala.model;

public enum ApLogType {

	BATCH("batch"), UI("ui"), TPIPAS("tpipas"), API("api");

	private String value;

	private ApLogType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
