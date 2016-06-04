/**
 * http://www.mkyong.com/java/jackson-2-convert-java-object-to-from-json/
 */
package com.pic.ala.model;

public class BatchJob extends Event {

	public BatchJob(final String sysID) {
		super(sysID, ApLogType.BATCH.getValue(), BatchJob.class);
	}

}