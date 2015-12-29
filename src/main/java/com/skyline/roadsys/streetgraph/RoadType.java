package com.skyline.roadsys.streetgraph;

/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public enum RoadType {
	PRIMARY(0), SECONDARY(1);
	private int value;
	private double width = 1d;

	RoadType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getWidth() {
		return this.width;
	}
}
