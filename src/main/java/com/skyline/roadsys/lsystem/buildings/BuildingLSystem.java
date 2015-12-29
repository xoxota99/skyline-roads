package com.skyline.roadsys.lsystem.buildings;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.rendering.*;

public class BuildingLSystem extends GraphicLSystem {

	public Lot parentLot;
	/**
	 * Maximum height together with the lot in which this building resides
	 * defines bounding box.
	 **/
	protected Shape boundingBox;

	protected List<Shape> boundingBoxStack;

	public BuildingLSystem(Lot parentLot) {
		setParentLot(parentLot);
		initialize();
	}

	private void setParentLot(Lot parentLot) {
		this.parentLot = parentLot;
	}

	public BuildingLSystem(BuildingLSystem source) {
		initialize();
		boundingBox = (source.boundingBox);
	}

	public double maxHeight() {
		return boundingBox.height();
	}

	public void setMaxHeight(double maxHeight) {
		boundingBox.setHeight(maxHeight);
	}

	public void pushBoundingBox() {
		boundingBoxStack.add(boundingBox);
	}

	public void popBoundingBox() {
		if (boundingBoxStack.size() > 0) {

			boundingBox = boundingBoxStack.get(boundingBoxStack.size() - 1);
			// delete boundingBoxStack.back();
			boundingBoxStack.remove(boundingBoxStack.size() - 1);
		}
	}

	protected void interpretSymbol(char symbol)
	{
		switch (symbol)
		{
		case '{':
			pushBoundingBox();
			break;
		case '}':
			popBoundingBox();
			break;
		default:
			/* Try to interpret symbols defined in parent. */
			super.interpretSymbol(symbol);
			break;
		}
	}

	protected void initialize() {
		Polygon area = parentLot.areaConstraints();
		boundingBox = new Shape();
		boundingBox.setBase(area);
		boundingBox.setHeight(0);

		addToAlphabet("{}");

		setInitialPosition(area.centroid());
		setInitialDirection(new Vector3d(0, 0, 1));

	}

}
