package com.pic.ala.gen;

public class BatchJobCommand extends Thread implements Command {

	private BatchJob batchJob;

	public BatchJobCommand(BatchJob batchJob) {
		this.batchJob = batchJob;
	}

	@Override
	public void run() {
		batchJob.run();
	}

}