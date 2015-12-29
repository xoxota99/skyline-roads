package com.skyline.building;

import javax.vecmath.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.util.*;

public class RedBuilding extends Building {

	public RedBuilding(Lot parentLot) {
		super(parentLot);
		initialize();
	}

	protected void configure() {
		setAxiom("{BSFER}");	//Basement, Spacer, Storey, E, Roof

		// addRule('E', "FE"); // Next normal floor
		addRule('E', "LFE"); // Ledge, then floor
		// addRule('E', "R-FE"); // Setbacks

		setInitialDirection(new Vector3d(0, 0, 1));

		setMaxHeight(MathUtil.randomInt(20,30) * 2.5 * Units.METER);

		setupTextures();
	}

	protected void setupTextures() {

		switch (MathUtil.randomInt(0,3))
		{
			case 0:
				// windowTileMaterial = "Modern3Window";
				// basementMaterial = "StreetLevel3";
				// spacerMaterial = "Modern3Ledge";
				// ledgeMaterial = "Modern3Ledge";
				// rooftopMaterial = "OldRooftop";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 5 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				rooftopHeight = 4 * Units.METER;
				tileWidth = 10 * Units.METER;
				break;
			case 1:
				// windowTileMaterial = "ModernWindow2";
				// basementMaterial = "StreetLevel10";
				// spacerMaterial = "ModernLedge2";
				// ledgeMaterial = "ModernLedge2";
				// rooftopMaterial = "WhiteMetalRooftop";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 5 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				rooftopHeight = 4 * Units.METER;
				tileWidth = 8 * Units.METER;
				break;
			case 2:
				// windowTileMaterial = "ModernWindow";
				// basementMaterial = "StreetLevel5";
				// spacerMaterial = "ModernLedge";
				// ledgeMaterial = "ModernLedge";
				// rooftopMaterial = "WhiteMetalRooftop";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 4.8 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				break;
			case 3:
				// windowTileMaterial = "Brick2Window";
				// basementMaterial = "StreetLevel5";
				// spacerMaterial = "Brick2Ledge";
				// ledgeMaterial = "Brick2Ledge";
				// rooftopMaterial = "OldRooftop";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 4.8 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				break;
			default:
				throw new IllegalStateException("Wrong configuration.");
		}
	}

	protected void finishDrawing() {
		drawStorey();
		addBoundingBox(2);
		drawFloor();
		drawSpacer();
		drawRooftop();
	}

	protected void initialize() {
		configure();
	}

}
