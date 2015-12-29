package com.skyline.roadsys.area;

import java.util.*;

import javax.vecmath.*;

import org.apache.commons.logging.*;

import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.util.*;

public class Block extends Area {

	private Log logger = LogFactory.getLog(this.getClass());
	private List<Lot> lots;

	public Block() {
		initialize();
	}

	public Block(Zone parentZone) {
		initialize();
		setParent(parentZone);
	}

	public Block(Zone parentZone, Polygon border) {
		initialize();
		setParent(parentZone);
		setAreaConstraints(border);
	}

	public Block(Block source) {
		super(source);
		initialize();
	}

	/**
	 * Subdivision algorithm that subdivides a single block into a list of
	 * smaller areas - allotments.
	 * 
	 * @param[in] lotWidth Desired width of lots.
	 * @param[in] lotHeight Desired height of lots.
	 * @param[in] deviance Deviance factor in lot creation, such that 0.0 <= deviance <= 1.0.
	 */
	public void createLots(double lotWidth, double lotHeight, double deviance)
	{
		double LOT_WIDTH = lotWidth;
		double LOT_DEPTH = lotHeight;

		double LOT_DEVIANCE = deviance;
		if (deviance < 0 || deviance > 1) {
			throw new IllegalArgumentException("deviance must be between 0.0 and 1.0.");
		}

		SubRegion region; /* Current region. */
		/*
		 * Elements to be subdivided.
		 */
		List<SubRegion> regionQueue = new ArrayList<SubRegion>();
		/*
		 * Newly added regions by splitRegion.
		 */
		List<SubRegion> newRegions = new ArrayList<SubRegion>();
		/*
		 * Regions that need no further subdivision .
		 */
		List<SubRegion> outputRegions = new ArrayList<SubRegion>();

		SubRegion.Edge longestEdge; /* Longest edge of current region. */
		LineSegment edgeLine = new LineSegment();
		double splitSize;
		Point sp1, sp2; /* Split line. */

		/* A valid region must have at least 3 vertices. */
		if (constraints.numberOfVertices() < 3) {
			throw new IllegalArgumentException("Number of vertices in constraints must be >= 3.");
		}

		/*
		 * Convert areaConstraints of this block to polygonGraph that is used in
		 * subdivision algorithm.
		 */
		region = new SubRegion(constraints);

		SubRegion.Edge blockFirst = region.getFirstEdge();
		SubRegion.Edge current = blockFirst;
		do /* All edges of Block has road access */
		{
			current.hasRoadAccess = true;
			current = current.next;
		} while (current != blockFirst);

		/* Add the region to queue for splitting */
		regionQueue.add(region);

		/* While there are some more regions to subdivide. */
		while (!regionQueue.isEmpty())
		{
			/* Get first region. */
			region = regionQueue.get(regionQueue.size() - 1);

			/* Calc the longest road edge and split size. */
			longestEdge = region.getLongestEdgeWithRoadAccess();
			if (longestEdge != null)
			{
				edgeLine.set(new Point(longestEdge.begining), new Point(longestEdge.next.begining));
				logger.debug("Longest Road edge: " + edgeLine.length());
				if (edgeLine.length() <= LOT_WIDTH) /*
													 * No road edge requires
													 * further splitting.
													 */
				{
					/* Calc the longest non-road edge and split size. */
					longestEdge = region.getLongestEdgeWithoutRoadAccess();
					if (longestEdge != null)
					{
						edgeLine.set(longestEdge.begining, longestEdge.next.begining);
						logger.debug("Longest NONRoad edge: " + edgeLine.length());
						if (edgeLine.length() <= LOT_DEPTH) /*
															 * No non-road edge
															 * requires further
															 * splitting.
															 */
						{
							logger.debug("Region is small enough, moving to output.");
							logger.debug("  area = " + region.toPolygon().area());
							logger.debug(region.toString());
							/* Region is complete. */
							outputRegions.add(region);
							regionQueue.remove(regionQueue.get(regionQueue.size() - 1));
							continue;
						}
						else
						{
							splitSize = LOT_DEPTH;
						}
					}
					else
					{
						/* All edges are small enough */
						outputRegions.add(region);
						regionQueue.remove(regionQueue.get(regionQueue.size() - 1));
						continue;
					}
				}
				else
				{
					splitSize = LOT_WIDTH;
				}
			}
			else
			{
				// if lot is small enough, add completed region
				logger.debug("THIS REGION HAS NO Edge with RoadAccess.");
				logger.debug("  " + region.toPolygon().toString());
				outputRegions.add(region);
				regionQueue.remove(regionQueue.get(regionQueue.size() - 1));
				continue;
			}

			// calculate the split points
			sp1 = calcSplitPoint(edgeLine, splitSize, LOT_DEVIANCE);
			sp2 = new Point(sp1);
			sp2.add(edgeLine.normal());// *longestEdge.length();
			// split and process the new regions
			newRegions = splitRegion(region, sp1, sp2); // BUG: <-- not working?
			regionQueue.remove(regionQueue.get(regionQueue.size() - 1));
			for (SubRegion newRegion : newRegions)
			{
				logger.debug("New region:");
				logger.debug(newRegion.toString());
				if (newRegion.hasRoadAccess())
				{
					logger.debug("  Adding to processing queue: " + newRegion.toPolygon().toString());
					regionQueue.add(newRegion); // add to processing queue
				}
				else
				{
					logger.debug("  Discarded.");
				}
			}
		}

		Polygon newRegionPolygon;
		logger.debug("Block::createLots() numberOfRegions " + outputRegions.size());
		for (SubRegion newRegion : outputRegions)
		{
			newRegionPolygon = newRegion.toPolygon();
			if (newRegionPolygon.isNonSelfIntersecting())
			{
				lots.add(new Lot(this, newRegionPolygon));
			}
			else
			{
				throw new IllegalStateException("Self-intersecting polygon encountered: " + newRegionPolygon.toString());
			}
		}
	}

	public List<Lot> getLots() {
		return lots;
	}

	private Point calcSplitPoint(LineSegment longestEdge, double splitSize, double lotDeviance)
	{
		double factor, fraction, midPosition;

		logger.debug("Block::calcSplitPoint() longestEdge.length() = " + longestEdge.length());
		factor = Math.floor(longestEdge.length() / splitSize + 0.5);
		fraction = 1 / factor;
		logger.debug("Block::calcSplitPoint() factor = " + factor);
		midPosition = (factor / 2) * fraction;
		logger.debug("Block::calcSplitPoint() midPosition = " + midPosition);

		if (midPosition<=0 || midPosition>=1){
			throw new IllegalStateException("midPosition must be between 0.0 and 1.0: " + midPosition);
		}

		// calculate longest edge vector src . dst
		Vector3d longestEdgeDirection = new Vector3d(longestEdge.end());
		longestEdgeDirection.sub(longestEdge.begining());
		// Random numberGenerator;

		Point retval = new Point(longestEdgeDirection);
		retval.scale(midPosition + (lotDeviance * (MathUtil.random.nextDouble() - 0.5) * fraction));
		retval.add(longestEdge.begining());
		return retval;
	}

	private List<SubRegion> splitRegion(SubRegion area, Point a, Point b)
	{
		SubRegion.Edge region = area.getFirstEdge();
		Vector3d ab = new Vector3d(b);
		b.sub(a);
		double Lsq = ab.length() * ab.length();

		SubRegion.Edge edge = region;
		do
		{
			Vector3d ac = new Vector3d(edge.begining);
			ac.sub(a);
			edge.s = (-ac.y * ab.x + ac.x * ab.y) / Lsq;
			edge = edge.next;
		} while (edge != region);

		// Vector3d ca, cd;
		SubRegion.Edge intersection;
		List<SubRegion.Edge> createdEdges = new ArrayList<SubRegion.Edge>();
		IntersectionType result;
		Line splitLine = new Line(a, b);// BUG: This line is effed up.
		LineSegment currentEdge = new LineSegment();
		Point intersectionPoint = new Point();

		logger.debug("SPLITTING AREA: ");
		logger.debug(area.toString());

		edge = region;
		do
		{
			currentEdge.set(edge.begining, edge.next.begining);
			logger.debug("-------------------");
			logger.debug("splitLine " + splitLine.toString());
			logger.debug("currentEdge " + currentEdge.toString());
			logger.debug("Current edge roadAccess " + edge.hasRoadAccess);
			result = currentEdge.intersection2D(splitLine, intersectionPoint); // BYREF
																				// intersectionPoint
			if (result == IntersectionType.INTERSECTING)
			{
				logger.debug("Intersection at: " + intersectionPoint.toString());
				if (intersectionPoint.equals(currentEdge.begining()))
				{
					logger.debug("New edge = current edge");
					intersection = edge;
					intersection.hasRoadAccess = edge.hasRoadAccess;
					createdEdges.add(intersection);
				}
				else if (intersectionPoint.equals(currentEdge.end()))
				{
					// intersection.hasRoadAccess = edge.next.hasRoadAccess;
					// intersection = edge.next;
				}
				else if (!intersectionPoint.equals(currentEdge.begining()) &&
						!intersectionPoint.equals(currentEdge.end()))
				{
					logger.debug("begin diff x = " + (intersectionPoint.x - currentEdge.begining().x));
					logger.debug("begin diff y = " + (intersectionPoint.y - currentEdge.begining().y));
					logger.debug("end diff x = " + (intersectionPoint.x - currentEdge.end().x));
					logger.debug("end diff y = " + (intersectionPoint.y - currentEdge.end().y));
					intersection = area.insert(edge, intersectionPoint);
					intersection.hasRoadAccess = edge.hasRoadAccess;
					createdEdges.add(intersection);
					edge = edge.next; // Jump over the just inserted area
				}
			}

			edge = edge.next; // edge++;
		} while (edge != region);

		// sort the created list by location on ab
		Collections.sort(createdEdges);
		// createdEdges.sort();

		// mark edges as unvisited
		edge = region;
		do
		{
			edge.s = 0;
			edge = edge.next;
		} while (edge != region);

		List<SubRegion> outputRegions = new ArrayList<SubRegion>();

		logger.debug("Created edges: " + createdEdges.size());
		if (createdEdges.size() % 2 != 0)
		{
			// There was a problem with a subdivision
			return outputRegions; // throw the block away
		}

		List<SubRegion.Edge> temporary = new ArrayList<SubRegion.Edge>(createdEdges);
		for (int i = 0; i < temporary.size(); i += 2) /* Step by two */
		{
			area.bridge(temporary.get(i), temporary.get(i + 1));
		}

		// finally extract the new regions
		boolean skipDuplicate;
		for (SubRegion.Edge createdEdge : createdEdges)
		{
			edge = createdEdge;
			skipDuplicate = false;
			do
			{
				if (edge.s > 0)
				{
					skipDuplicate = true;
					break;
				}
				edge.s = 1; // mark edge as visited
				edge = edge.next; // advance to next edge
			} while (edge != createdEdge); // until we come full circle.

			if (!skipDuplicate)
			{
				outputRegions.add(new SubRegion(edge));
			}
		}

		logger.debug("Block::splitRegion(): returning regions: " + outputRegions.size());
		return outputRegions;
	}

	private void initialize() {
		if (lots != null) {
			lots.clear();
		} else {
			lots = new ArrayList<Lot>();
		}
	}

}
