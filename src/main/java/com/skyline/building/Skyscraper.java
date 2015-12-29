package com.skyline.building;

import javax.vecmath.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.util.*;

public class Skyscraper extends Building {

	public Skyscraper(Lot parentLot) {
		super(parentLot);
		initialize();
	}

	protected void configure() {
		addToAlphabet("C"); //setback

		setAxiom("{BSFER}");	//Basement, Spacer, Storey, E, Roof

		addRule('E', "FE"); // Storey, E
		addRule('E', "SFE"); // Spacer, Storey, E
		addRule('E', "GCFE"); // Floor, Setback, Storey, E

		setInitialDirection(new Vector3d(0, 0, 1));

		setMaxHeight(MathUtil.randomInt(20,30) * 2.5 * Units.METER);

		setupTextures();
	}

	protected void setupTextures() {

		switch (MathUtil.randomInt(0,2))
		{
			case 0:
				// windowTileMaterial = "HotelWindow";
				// basementMaterial = "StreetLevel2";
				// spacerMaterial = "HotelLedge";
				// ledgeMaterial = "HotelLedge";
				// rooftopMaterial = "DirtyRooftop1";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 4 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				break;
			case 1:
				// windowTileMaterial = "OfficeBuildingWindow";
				// basementMaterial = "StreetLevel7";
				// spacerMaterial = "OfficeBuildingLedge";
				// ledgeMaterial = "OfficeBuildingLedge";
				// rooftopMaterial = "DirtyRooftop1";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 4 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				break;
			case 2:
				// windowTileMaterial = "GlassTowerWindow";
				// basementMaterial = "StreetLevel7";
				// spacerMaterial = "GlassTowerLedge";
				// ledgeMaterial = "GlassTowerLedge";
				// rooftopMaterial = "SkyScraperRooftop";

				storeyHeight = 2.5 * Units.METER;
				basementHeight = 4 * Units.METER;
				spacerHeight = 1 * Units.METER;
				ledgeHeight = 1 * Units.METER;
				tileWidth = 10 * Units.METER;
				break;
			default:
				throw new IllegalStateException("Wrong configuration.");
		}
	}

	protected void setback() {
		if (cursor.getPosition().z >= 0.6 * maxHeight())
		{
			substractBoundingBox(5);
		}
	}

	protected void interpretSymbol(char symbol) {

		  switch (symbol)
		  {
		    case 'C': /* Setback */
		      setback();
		      break;
		    default:
		      /* Try to interpret symbols defined in parent. */
		      super.interpretSymbol(symbol);
		      break;
		  }
	}

	protected void initialize() {
		configure();
	}
}
