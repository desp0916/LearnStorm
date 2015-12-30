package com.pic.ala.gen;

public class BatchJobCommand extends Thread implements Command {

	private BatchJob batchJob;

	public BatchJobCommand(BatchJob batchJob) {
		this.batchJob = batchJob;
		this.setName(batchJob.getSystemId() + "-BatchJobCommand");
	}

	@Override
	public void run() {
		batchJob.run();
	}

}