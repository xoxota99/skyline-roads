package com.skyline.roadsys.area;

import com.skyline.roadsys.geometry.*;
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public abstract class Area {
	protected Polygon constraints;
	protected Area parentArea;

	public Area() {
		initialize();
	}

	public Area(Area source) {
		initialize();
		constraints = source.constraints;
		parentArea = source.parentArea;
	}

	public void setAreaConstraints(Polygon area) {
		constraints = area;
	}

	public Polygon areaConstraints() {
		return constraints;
	}

	public void setParent(Area area) {
		this.parentArea = area;
	}

	public Area parent() {
		return parentArea;
	}

	private void initialize() {
		parentArea = null;
		constraints = new Polygon();
	}
	
	public String toString(){
		return "Area: " + constraints.toString();
	}

}
