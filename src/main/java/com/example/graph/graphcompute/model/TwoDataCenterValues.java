package com.example.graph.graphcompute.model;

public class TwoDataCenterValues {
	private String dc1;
	private String dc2;
	private double latency;
	private double bandwidth;

	public TwoDataCenterValues() {

	}

	public TwoDataCenterValues(String dc1, String dc2, double latency, double bandwidth) {

		this.dc1 = dc1;
		this.dc2 = dc2;
		this.latency = latency;
		this.bandwidth = bandwidth;
	}

	public String getDc1() {
		return dc1;
	}

	public void setDc1(String dc1) {
		this.dc1 = dc1;
	}

	public String getDc2() {
		return dc2;
	}

	public void setDc2(String dc2) {
		this.dc2 = dc2;
	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

	public double getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}

}
