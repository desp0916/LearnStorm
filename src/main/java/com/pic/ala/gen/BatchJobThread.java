package com.pic.ala.gen;

public class BatchJobThread extends Thread implements Command {

	private BatchJob batchJob;

	public BatchJobThread(final BatchJob batchJob) {
		this.batchJob = batchJob;
		this.setName(batchJob.getSystemId() + "-BatchJob");
	}

	@Override
	public void run() {
		batchJob.run();
	}

}