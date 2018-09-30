package com.example.graph.graphcompute.model;

/* sum of total values and count to compute average */

public class ValCount {
	private double latency = 0;
	private int count = 0;

	public void increaseVal(double latency) {
		this.latency += latency;
		this.count += 1;
	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
