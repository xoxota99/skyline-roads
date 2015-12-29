package com.skyline.roadsys;

import java.util.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.streetgraph.*;

public abstract class City {

	protected StreetGraph map;
	protected List<Zone> zones;
	protected Polygon area;

	public City() {
		area = new Polygon();
		map = new StreetGraph();
		zones = new ArrayList<Zone>();
	}

	public void generate() {
		createPrimaryRoadNetwork();
		createZones();
		createSecondaryRoadNetwork();
		createBlocks();
		createBuildings();
	}

	protected abstract void createBuildings();

	protected abstract void createBlocks();

	protected abstract void createSecondaryRoadNetwork();

	protected abstract void createZones();

	protected abstract void createPrimaryRoadNetwork();
}
