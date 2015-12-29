package com.skyline.roadsys.area;

import com.skyline.roadsys.geometry.*;

public class Lot extends Area {

	public Lot() {
	}

	public Lot(Block parentBlock, Polygon area) {
		setParent(parentBlock);
		setAreaConstraints(area);
	}

	public Lot(Lot source) {
		super(source);
	}

}
