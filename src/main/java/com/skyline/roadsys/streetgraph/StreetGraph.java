package com.skyline.roadsys.streetgraph;

import java.util.*;

import org.apache.commons.logging.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;

/**
 * Street graph representation
 * 
 * Roads and Intersections form together an undirected plannar graph. There are
 * two levels of the graph.
 * 
 * On a *topological* level, the streetgraph says where a certain road begins
 * and where it leads to. On the lower level, the *geometrical* level, the graph
 * says where exactly in the space are the topological elements (Intersection,
 * Roads) located by specifying position (Point) for an intersection and path
 * (Line) for a road.
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class StreetGraph {
	private List<Road> roads;
	private List<Intersection> intersections;
	private Log logger = LogFactory.getLog(this.getClass());

	public StreetGraph() {
		initialize();
	}

	private void initialize() {
		roads = new ArrayList<Road>();
		intersections = new ArrayList<Intersection>();
	}

	public List<Zone> findZones() {
		logger.debug("StreetGraph::findZones() passing " + intersections.size() + " intersections to MCB.");
		AreaExtractor graph = new AreaExtractor();
		return graph.extractZones(this, null);
	}

	/**
	 * If there is a road between the given intersections, return it.
	 * 
	 * @param first
	 *            The first intersection
	 * @param second
	 *            The second intersection
	 * @return the road between the first and second intersections, or null if
	 *         none exists.
	 */
	public Road getRoadBetweenIntersections(Intersection first, Intersection second)
	{
		Set<Road> roadsOfFirst = first.getRoads();

		for (Road road : roadsOfFirst) {
			if ((road.getFrom().equals(first) && road.getTo().equals(second)) ||
					(road.getFrom().equals(second) && road.getTo().equals(first)))
			{
				return road;
			}
		}

		return null;
	}

	/**
	 * Equivalent to {@link #addRoad(path, RoadType.PRIMARY)}
	 * 
	 * @param path
	 *            Path of the new road (low level part of the graph).
	 */
	public void addRoad(Path path) {
		this.addRoad(path, RoadType.PRIMARY);
	}

	/**
	 * Add road that follows certain path into the StreetGraph.
	 * 
	 * This method makes sure that the graph is planar so more than one road may
	 * be added at a time. If an intersection with an existing road is detected,
	 * the new road is split into two.
	 * 
	 * @param path
	 *            Path of the new road (low level part of the graph).
	 * @param roadType
	 *            the type of road to add.
	 */
	public void addRoad(Path path, RoadType roadType)
	{
		Path roadPath = new Path(path);
		Point intersection = new Point();
		// O(n) check to determine if the proposed path intersects any of the
		// existing roads.
		for (Road currentRoad : roads) {
			// Check for intersection
			// LineSegment::Intersection
			IntersectionType intersectionType = roadPath.crosses(currentRoad.getPath(), intersection); // BYREF:intersection
			if (intersectionType != IntersectionType.ORTHOGONAL) {
				if (intersectionType == IntersectionType.CONTAINED ||
						intersectionType == IntersectionType.IDENTICAL ||
						intersectionType == IntersectionType.CONTAINING ||
						intersectionType == IntersectionType.OVERLAPPING)
				{
					logger.error("Attempted to add a road that is "+intersectionType.toString() + " with another road.");
					return;
					// throw new
					// IllegalStateException("Attempted to add a road that is CONTAINED, IDENTICAL, CONTAINING, or OVERLAPPING another road.");
				}

				if (intersectionType == IntersectionType.INTERSECTING)
				{

					if (intersection.equals(roadPath.begining()) ||
							intersection.equals(roadPath.end()))
					/* New road is just touching some other one */
					{
					}
					else
					{
						Path firstPart = new Path(new LineSegment(roadPath.begining(), intersection)), secondPart = new Path(new LineSegment(intersection, roadPath.end()));
						addRoad(firstPart, roadType);
						addRoad(secondPart, roadType);

						return;
					}
				}
			}
		}

		Intersection begining = addIntersection(roadPath.begining());
		Intersection end = addIntersection(roadPath.end());

		Road newRoad = new Road(begining, end);
		newRoad.setType(roadType);
		logger.info(roadType.toString() + " - " + begining.toString() + "-" + end.toString());
		// newRoad.setPath(roadPath);

		// Connect road to intersections
		begining.connectRoad(newRoad);
		end.connectRoad(newRoad);

		roads.add(newRoad);
	}

	/**
	 * Erase road from the StreetGraph.
	 * 
	 * Road is disconnected from both Intersections. If there is no use for them
	 * (they have no roads leading to them) they will be removed as well.
	 * 
	 * @param road
	 *            Road to remove.
	 */
	public void removeRoad(Road road) {
		Intersection begining = road.getFrom();
		Intersection end = road.getTo();

		begining.disconnectRoad(road);
		if (begining.numberOfWays() == 0)
		{
			intersections.remove(begining);
		}

		end.disconnectRoad(road);
		if (end.numberOfWays() == 0)
		{
			intersections.remove(end);
		}

		roads.remove(road);
	}

	/**
	 * Method for adding new intersections to the graph.
	 * 
	 * If some intersection exists at the target position no intersection is
	 * added (the existing one is returned). Also if the new intersection
	 * resides on existing road, the road will be split it in two and connected
	 * through the new intersection.
	 * 
	 * @param position
	 *            Where to put the intersection.
	 * @return Intersection at position specified by point (new or existing).
	 */
	private Intersection addIntersection(Point position)
	{
		/* Search for existing intersection. */
		for (Intersection intersection : intersections) {
			if (intersection.position().equals(position))
			{
				return intersection;
			}
		}

		/* There's no existing intersection at position. Create one */
		Intersection newIntersection = new Intersection(position);
		intersections.add(newIntersection);

		logger.debug("addIntersection(): Adding intersection @ " + newIntersection.position().toString());

		/* Check if the existing intersection crosses any existing road. */

		for (Road road : roads) {
			if (road.getPath().goesThrough(position))
			/* If so, split the road into two. */
			{
				logger.debug("addIntersection(): Splitting road for Intersection @ " + newIntersection.position().toString());

				Intersection end = road.getTo();

				// WTF: What's so special about these coordinates?
				// assert (!(new LineSegment(road.begining().position(),
				// end.position()).equals(new LineSegment(
				// new Point(-3000, -2509.3, 0),
				// new Point(-3000, -2244.59, 0)))));
				end.disconnectRoad(road);

				road.setTo(newIntersection);
				newIntersection.connectRoad(road);

				Road secondPart = new Road(newIntersection, end);
				secondPart.setType(road.getType());
				roads.add(secondPart);

				newIntersection.connectRoad(secondPart);
				end.connectRoad(secondPart);
				// WTF: and again.
				// assert (!(new LineSegment(road.begining().position(),
				// end.position()).equals(new LineSegment(new Point(-3000,
				// -2509.3, 0), new Point(-3000, -2244.59, 0)))));
				// WTF: and again....
				// assert (!(new LineSegment(newIntersection.position(),
				// end.position()).equals(new LineSegment(new Point(-3000,
				// 837.305, 0), new Point(-3000, -2509.3, 0)))));

				break;
			}
		}

		return newIntersection;
	}

	public boolean isIntersectionAtPosition(final Point position)
	{
		/* Search for existing intersection. */
		for (Intersection intersection : intersections)
		{
			if (intersection.position().equals(position))
			{
				return true;
			}
		}

		return false;
	}

	public Intersection getIntersectionAtPosition(final Point position)
	{
		/* Search for existing intersection. */
		for (Intersection intersection : intersections)
		{
			if (intersection.position().equals(position))
			{
				return intersection;
			}
		}

		return null;
	}

	public int numberOfRoads() {
		return roads.size();
	}

	public int numberOfIntersections() {
		return intersections.size();
	}

	public List<Road> getRoads() {
		return roads;
	}

	public void setRoads(List<Road> roads) {
		this.roads = roads;
	}

	public List<Intersection> getIntersections() {
		return intersections;
	}

	public void setIntersections(List<Intersection> intersections) {
		this.intersections = intersections;
	}

	/**
	 * Utility function, to verify integrity of the graph.
	 */
	private void checkConsistency()
	{
		Point intersection = new Point();
		for (Road currentRoad : roads)
		{
			for (Road nextRoad : roads)
			{
				if (nextRoad.equals(currentRoad))
					continue;

				// Check for intersection
				IntersectionType intersectionType = currentRoad.getPath().crosses(nextRoad.getPath(), intersection); // BYREF:
				// intersection
				if (intersectionType == IntersectionType.INTERSECTING)
				{

					if (intersection.equals(currentRoad.getPath().begining()) ||
							intersection.equals(currentRoad.getPath().end()))
					/* New road is just touching some other one */
					{
					}
					else
					{
						assert (false); // WTF?
					}
				}
				else if (intersectionType == IntersectionType.CONTAINED)
				{
					assert (false);
				}
				else if (intersectionType == IntersectionType.IDENTICAL)
				{
					assert (false);
				}
				else if (intersectionType == IntersectionType.CONTAINING)
				{
					assert (false);
				}
				else if (intersectionType == IntersectionType.OVERLAPPING)
				{
					assert (false);
				}
			}
		}
	}

	/**
	 * Remove all dead-end roads from the street graph.
	 */
	public void removeFilamentRoads()
	{
		Road currentRoad;
		Intersection roadBegining;
		Intersection roadEnd;
		List<Road> filaments = new ArrayList<Road>();

		for (Road road : roads)
		{
			currentRoad = road;
			roadBegining = currentRoad.getFrom();
			roadEnd = currentRoad.getTo();
			if (roadBegining.numberOfWays() <= 1 ||
					roadEnd.numberOfWays() <= 1)
			{
				filaments.add(currentRoad);
			}
		}

		for (Road filament : filaments)
		{
			removeRoad(filament);
		}
	}

	public String toString() {
		StringBuffer output = new StringBuffer();
		output.append("Roads:\n");
		for (Road road : roads)
		{
			output.append("  " + road + "\n");
			output.append("    from " + road.getFrom() + " to " + road.getTo() + "\n");
			output.append("    " + road.getPath().toString() + "\n");
		}

		output.append("Intersections:\n");
		for (Intersection intersection : intersections)
		{
			output.append("  " + intersection + "\n");
			output.append("    at " + intersection.position().toString() + "\n");

			Set<Road> intersectionRoads = intersection.getRoads();
			for (Road road : intersectionRoads)
			{
				output.append("    " + road + "\n");
			}
		}

		return output.toString();
	}

}
