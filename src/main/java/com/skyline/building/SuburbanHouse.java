package com.skyline.building;

import javax.vecmath.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.util.*;

public class SuburbanHouse extends Building {
	public SuburbanHouse(Lot parentLot) {
		super(parentLot);
		initialize();
	}

	protected void configure() {
		  setAxiom("{--BF+GH}"); // Left, Left, Basement, Storey, Right, Floor, HouseRooftop

		  addRule('E', "FE");   // Storey, E
		  //addRule('E', "R-FE"); // Setbacks

		  setInitialDirection(new Vector3d(0,0,1));

		  setupTextures();
	}

	protected void setupTextures() {
		  switch (MathUtil.randomInt(0,1))
		  {
		    case 0:
//		      windowTileMaterial = "SuburbanFloor";
//		      basementMaterial   = "SuburbanStreetLevel";
//		      spacerMaterial     = "BrickLedge";
//		      ledgeMaterial      = "BrickLedge";
//		      rooftopMaterial    = "WoodenRooftop";

		      storeyHeight   = 2.5 * Units.METER;
		      basementHeight = 4 * Units.METER;
		      spacerHeight   = 1 * Units.METER;
		      ledgeHeight    = 1 * Units.METER;
		      tileWidth      = 10 * Units.METER;
		      rooftopHeight  = 4 * Units.METER;
		      break;
		    case 1:
//		      windowTileMaterial = "SuburbanResidenceFloor";
//		      basementMaterial   = "SuburbanResidenceFloor";
//		      spacerMaterial     = "BrickLedge";
//		      ledgeMaterial      = "BrickLedge";
//		      rooftopMaterial    = "WoodenRooftop";

		      storeyHeight   = 2.5 * Units.METER;
		      basementHeight = 4 * Units.METER;
		      spacerHeight   = 1 * Units.METER;
		      ledgeHeight    = 1 * Units.METER;
		      tileWidth      = 10 * Units.METER;
		      rooftopHeight  = 4 * Units.METER;
		      break;
		    default:
		      throw new IllegalStateException("Wrong configuration.");
		  }
	}

	protected void finishDrawing() {
		//do nothing.
	}

	protected void initialize() {
		configure();
	}

}
