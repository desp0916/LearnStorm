/**
 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
 */
package com.pic.ala.model;

public class TPIPASEvent extends Event {

	public TPIPASEvent(final String sysID) {
		super(sysID, ApLogType.TPIPAS.getValue(), TPIPASEvent.class);
	}

}