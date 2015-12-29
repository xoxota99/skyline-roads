package com.skyline.roadsys.area;

import java.util.*;

import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.roads.*;
import com.skyline.roadsys.streetgraph.*;

/*
 Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class Zone extends Area {

	private RoadLSystem roadGenerator;
	private StreetGraph associatedStreetGraph;

	private List<Block> blocks;

	private void initialize() {
		associatedStreetGraph = null;
		roadGenerator = null;
		constraints = new Polygon();
		blocks = new ArrayList<Block>();
	}

	public Zone(StreetGraph streets) {
		initialize();
		associatedStreetGraph = streets;
	}

	public Zone(Zone source) {
		initialize();
		associatedStreetGraph = source.associatedStreetGraph;
		constraints = source.constraints;
		roadGenerator = source.roadGenerator;
	}

	public void setRoadGenerator(RoadLSystem generator) {
		this.roadGenerator = generator;
	}

	public StreetGraph streetGraph() {
		return associatedStreetGraph;
	}

	public void setStreetGraph(StreetGraph streets) {
		associatedStreetGraph = streets;
	}

	public boolean isIntersectionInside(Intersection intersection) {
		return constraints.encloses2D(intersection.position());
	}

	public boolean roadIsInside(Road road) {
		return isIntersectionInside(road.getFrom()) && isIntersectionInside(road.getTo());
	}

	public void createBlocks() {
		AreaExtractor extractor = new AreaExtractor();
		blocks = extractor.extractBlocks(associatedStreetGraph, this);
	}

	public List<Block> getBlocks() {
		return blocks;
	}
}
