package com.skyline.roadsys.streetgraph;

import com.skyline.roadsys.geometry.*;

/**
 * Represents an edge in the Streetgraph
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class Road {

	private Intersection from = null;
	private Intersection to = null;
	private Path path = null;
	private RoadType roadType;

	private void estimatePath() {
		path = new Path(new LineSegment(from.position(), to.position()));
	}

	public Road() {
		path = new Path();
	}

	public Road(Intersection from, Intersection to) {
		this.from = from;
		this.to = to;
		estimatePath();
	}

	public Road(Path path) {
		this.path = new Path(path); // make a copy.
	}

	public Intersection getFrom() {
		return from;
	}

	public Intersection getTo() {
		return to;
	}

	public Path getPath() {
		return path;
	}

	public void setType(RoadType roadType) {
		this.roadType = roadType;
	}

	public void setFrom(Intersection beginning) {
		this.from = beginning;
		path.setBeginning(beginning.position());
	}

	public void setTo(Intersection end) {
		this.to = end;
		path.setEnd(end.position());
	}

	public RoadType getType() {
		return roadType;
	}

	public String toString() {
		return "Road(" + getPath().toString() + ")";
	}

	/**
	 * Two roads are equivalent if:
	 * <ul><li>Road types are the same</li><li>endpoints are equivalent (including matching all endpoint child roads</li></ul>
	 */
	
	public boolean equals(Road road) {
		return road.from.equals(this.from)
				&& road.to.equals(this.to)
				&& road.roadType == this.roadType;
	}
}
