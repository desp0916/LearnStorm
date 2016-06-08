package com.pic.ala;

public class TR {
	public static int retryNum = 1;
	public static double retryDelayMultiplier = 1.2;
	public static double retryInitialDelayMs = 60 * 1000;

	public TR(int retryNum) {
		this.retryNum = retryNum;
	}

	public static void main(String[] args) {
		for (int r = 0; r < 10; r++) {
			System.out.println(Math.pow(retryDelayMultiplier, r));
			double delayMultiplier = Math.pow(retryDelayMultiplier, retryNum - 1);
			double delay = retryInitialDelayMs * delayMultiplier;
			System.out.println("delayMultiplier: " + delayMultiplier + ", delay: " + delay);
		}
	}
}
