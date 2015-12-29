package com.skyline.building;

import javax.vecmath.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.util.*;

public class OldBuilding extends Building {

	public OldBuilding(Lot parentLot) {
		super(parentLot);
		initialize();
	}

	protected void configure() {
		setAxiom("{BOFER}");	//Basement, ObliqueLedge, Storey, E, Roof

		addRule('E', "FE"); // Storey, E
		addRule('E', "FOFE"); // Storey, ObliqueLedge, Storey, E
		// addRule('E', "R-FE"); // Setbacks

		setInitialDirection(new Vector3d(0, 0, 1));

		setMaxHeight(MathUtil.randomInt(3,7) * 2.5 * Units.METER);

		setupTextures();
	}

	protected void setupTextures() {
		switch (MathUtil.randomInt(0,2))
		{
			case 0:
				// windowTileMaterial = "BrickWindow";
				// basementMaterial = "StreetLevel1";
				// spacerMaterial = "BrickLedge";
				// ledgeMaterial = "BrickLedge";
				// rooftopMaterial = "RedRooftop";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 4 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				rooftopHeight = 4 * Units.METER;
				break;
			case 1:
				// windowTileMaterial = "HistoricWindow";
				// basementMaterial = "StreetLevel3";
				// spacerMaterial = "HistoricLedge";
				// ledgeMaterial = "HistoricLedge";
				// rooftopMaterial = "RedRooftop";

				storeyHeight = 5 * Units.METER;
				basementHeight = 5 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				rooftopHeight = 4 * Units.METER;
				break;
			case 2:
				// windowTileMaterial = "Brick3Window";
				// basementMaterial = "StreetLevel6";
				// spacerMaterial = "Brick3Ledge";
				// ledgeMaterial = "Brick3Ledge";
				// rooftopMaterial = "RedRooftop";

				storeyHeight = 5 * Units.METER;
				basementHeight = 4.5 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				rooftopHeight = 4 * Units.METER;
				break;
			default:
				throw new IllegalStateException("Wrong configuration.");
		}
	}

	protected void finishDrawing() {
		drawObliqueLedge();
		addBoundingBox(2);
		drawHouseRooftop();
	}

	protected void initialize() {
		configure();
	}
}
