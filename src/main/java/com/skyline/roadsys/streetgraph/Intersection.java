package com.skyline.roadsys.streetgraph;

import java.util.*;

import org.apache.commons.logging.*;

import com.skyline.roadsys.geometry.*;

/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */
// TODO: Call this something else, like Vertex.
public class Intersection implements Comparable<Intersection> {
	private Set<Road> roads;
	private Point position;

	private Log logger = LogFactory.getLog(this.getClass());

	private Intersection() {
		// roads = new ArrayList<Road>();
		// position = new Point();
	}

	public Intersection(Point coordinates) {
		roads = new HashSet<Road>();
		position = new Point(coordinates);
	}

	public List<Intersection> adjacentIntersections()
	{
		List<Intersection> adjacent = new ArrayList<Intersection>();

		for (Road adjacentRoadIterator : roads)
		{
			if (adjacentRoadIterator.getFrom() != this)
			{
				adjacent.add(adjacentRoadIterator.getFrom());
			}
			else
			{
				adjacent.add(adjacentRoadIterator.getTo());
			}
		}

		// logger.debug("Intersection::adjacentIntersections(): has " +
		// numberOfWays() + " ways.");
		// logger.debug("Intersection::adjacentIntersections(): returns " +
		// adjacent.size() + " adjacent intersections.");
		return adjacent;
	}

	public void connectRoad(Road road)
	{
		if (road.getFrom().position().equals(position) ||
				road.getTo().position().equals(position))
		{
			roads.add(road);
		}
		else
		{
			throw new IllegalArgumentException("Tried to connect road " + road.toString() + " to Intersection at " + position);
		}
	}

	public Set<Road> getRoads() {
		return roads;
	}

	public void disconnectRoad(Road road) {
		roads.remove(road);
	}

	public int numberOfWays() {
		return roads.size();
	}

	public Point position() {
		return position;
	}

	public boolean hasRoad(Road road)
	{
		return roads.contains(road);
	}

	public boolean equals(Intersection other) {
		return roads.containsAll(other.roads)
				&& other.roads.containsAll(roads)
				&& position.equals(other.position);
	}

	/**
	 * Compares based SOLELY on X/Y position. Not sure if this is strictly
	 * correct.
	 */
	@Override
	public int compareTo(Intersection other) {
		return position.compareTo(other.position);
	}

//	public int hashCode() {
//		double hash = 17;
//
//		hash = hash * 23 + position.x;
//		hash = hash * 23 + position.y;
//		hash = hash * 23 + position.z;
//
//		return (int) hash;
//	}

	/**
	 * for ByRef copies.
	 * 
	 * @param clockwiseMost
	 */
	public void set(Intersection other) {
		this.position = new Point(other.position);
		this.roads = new HashSet<Road>(other.roads);
	}

	public String toString() {
		return position.toString();
	}
}
