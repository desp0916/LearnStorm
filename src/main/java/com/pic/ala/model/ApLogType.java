package com.pic.ala.model;

/**
 * AP Log 的類型
 *
 * @author gary
 * @since  2017年5月22日 下午2:59:32
 */
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
