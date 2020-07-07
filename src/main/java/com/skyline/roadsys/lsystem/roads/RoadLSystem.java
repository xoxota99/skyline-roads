package com.skyline.roadsys.lsystem.roads;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.rendering.*;
import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.util.*;

/**
 * Base class for Road generators
 * 
 * Alphabet:
 * <ul>
 * <li>- Turn left</li>
 * <li>+ Turn right</li>
 * <li>E Growth control</li>
 * <li>[ push current position</li>
 * <li>] pop current position</li>
 * <li>. draw point</li>
 * <li>_ - draw line forward</li>
 * </ul>
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class RoadLSystem extends GraphicLSystem {

	private static final double MINIMAL_ROAD_LENGTH = 100;
	private int generatedRoads;
	private StreetGraph targetStreetGraph;
	private Polygon areaConstraints;

	private double snapDistance;

	private RoadType generatedType;
	private double minRoadLength;
	private double maxRoadLength;
	private double minTurnAngle;
	private double maxTurnAngle;

	public RoadLSystem() {

		generatedRoads = 0;
		targetStreetGraph = null;
		areaConstraints = null;

		/*
		 * Symbols: - - turn left + - turn right E - growth control
		 */
		addToAlphabet("-+E");
		setAxiom("E"); // Grow.

		generatedType = RoadType.PRIMARY;
		minRoadLength = 0;
		maxRoadLength = 0;
		minTurnAngle = 0;
		maxTurnAngle = 0;
	}

	public boolean generateRoads(int number) {
		double targetNumberOfRoads = generatedRoads + number;
		boolean returnValue = true;
		while (generatedRoads < targetNumberOfRoads && returnValue) {
			returnValue = readNextSymbol() != 0; // TODO: Test this.
		}

		return returnValue;
	}

	public void generate() {
		logger.info("generating from Axiom: " + axiom);
		while (readNextSymbol() != 0) {
			// WTF
		}
	}

	public void setTarget(StreetGraph target) {
		targetStreetGraph = target;
	}

	public void setAreaConstraints(Polygon polygon) {
		areaConstraints = polygon;
	}

	public void setRoadType(RoadType type) {
		generatedType = type;
	}

	public void setRoadLength(double min, double max) {
		minRoadLength = min;
		maxRoadLength = max;
	}

	public void setTurnAngle(double min, double max) {
		minTurnAngle = min;
		maxTurnAngle = max;
	}

	public void setSnapDistance(double distance) {
		snapDistance = distance;
	}

	protected boolean isPathInsideAreaConstraints(Path proposedPath) {
		boolean beginningIsInside = areaConstraints.encloses2D(proposedPath.beginning()),
				endIsInside = areaConstraints.encloses2D(proposedPath.end());

		if (!beginningIsInside && !endIsInside) {
			return false;
		}

		Point intersection = new Point();
		LineSegment edge = new LineSegment();
		int vertices = areaConstraints.numberOfVertices();
		boolean touching = false;

		for (int number = 0; number < vertices; number++) {
			edge.setBeginning(areaConstraints.vertex(number));
			edge.setEnd(areaConstraints.vertex((number + 1) % vertices));

			if (edge.hasPoint2D(proposedPath.beginning()) || edge.hasPoint2D(proposedPath.end())) {
				touching = true;
				continue;
			}

			if (proposedPath.crosses(new Path(edge), intersection) == IntersectionType.INTERSECTING) // BYREF
																										// intersection
			{

				if (!areaConstraints.encloses2D(proposedPath.beginning())) {
					proposedPath.setBeginning(intersection);
				} else {
					proposedPath.setEnd(intersection);
				}
				break;
			}
		}

		if (touching)
		/* Line is touching, but not crossing any borders => it's all out */
		{
			return false;
		}

		return true;
	}

	protected void interpretSymbol(char symbol) {
		switch (symbol) {
		case '-':
			turnLeft();
			break;
		case '+':
			turnRight();
			break;
		case '_':
			drawRoad();
			break;
		case 'E':
			// nothing just control character
			logger.debug("Do nothing.");
			break;
		default:
			/* Try to interpret symbols defined in parent. */
			super.interpretSymbol(symbol);
			break;
		}
	}

	protected void turnLeft() {
		logger.debug("turning LEFT");
		cursor.turn(-1 * getTurnAngle());
	}

	protected void turnRight() {
		logger.debug("turning RIGHT");
		cursor.turn(getTurnAngle());
	}

	protected void drawRoad() {

		Point previousPosition = new Point(cursor.getPosition());
		cursor.move(getRoadSegmentLength());
		Point currentPosition = new Point(cursor.getPosition());

		/* According to global goals */
		Path proposedPath = new Path(new LineSegment(previousPosition, currentPosition));
		logger.debug("drawing road from (" + previousPosition.toString() + ") to (" + currentPosition.toString() + ")");
		if (!isPathInsideAreaConstraints(proposedPath))
		/* Path is outside the area constraints */
		{
			logger.debug("	path is outside area constraints. Canceling branch.");
			cancelBranch();
			return;
		}

		/* Modify path according to localConstraints of existing streets. */
		logger.debug("applying Local constraints");
		if (!localConstraints(proposedPath)) // BAD.
		{
			logger.debug("	path violates local constraints. Canceling.");
			cancelBranch();
			return;
		}

		if (targetStreetGraph.isIntersectionAtPosition(proposedPath.end())) {
			logger.debug("	path ends at an existing intersection. Canceling.");
			// Don't branch into existing intersections
			cancelBranch();
			// FIXME: Why aren't we returning here?
		}

		/* Add path to the streetgraph */
		cursor.setPosition(proposedPath
				.end()); /*
							 * Set cursor position at the end of generated road.
							 */
		targetStreetGraph.addRoad(proposedPath, generatedType);
		generatedRoads++;
	}

	protected boolean localConstraints(Path proposedPath) {

		Path tempPath = new Path(proposedPath);
		Intersection nearestIntersection = null;
		double distanceToNearestIntersection = snapDistance + 1;

		Road nearestRoad = null;
		double distanceToNearestRoad = snapDistance + 1;
		boolean isClose = false;
		boolean snapped = false;

		Point intersection = new Point();
		double distance;
		for (Road currentRoad : targetStreetGraph.getRoads()) {
			// Check for intersection
			logger.trace("Checking for intersection between proposedPath (" + tempPath.toString()
					+ ") and road path (" + currentRoad.getPath() + ")");
			IntersectionType intersectionType = tempPath.crosses(currentRoad.getPath(), intersection); // BYREF:
																											// intersection
			assert (intersectionType != IntersectionType.INTERSECTING
					|| intersectionType != IntersectionType.ORTHOGONAL);
			logger.trace("	intersectionType " + intersectionType
					+ (intersection != null ? " at " + intersection.toString() : ""));

			// STEP 1: if the proposed road intersects with this one, create an
			// intersection (immediate snap)

			if (intersectionType == IntersectionType.INTERSECTING) {
				logger.trace("roads intersect. Cutting proposedPath at point " + intersection.toString());

				/* New road is just touching some other one */
				// we'll snap it anyways, since "equals" in this case just means
				// within some epsilon value, and not EXACTLY.
				if (intersection.equals(tempPath.begining())) {
					// tempPath.begining().set(intersection); //may not be a
					// good idea to modify the "beginning" value. Beginning is
					// supposed to be anchored.
				} else if (intersection.equals(tempPath.end())) {
					tempPath.setEnd(intersection);
				} else
				/* Cut off the end of the path. */
				{
					tempPath.setEnd(intersection);
					continue;
				}
			}

			// STEP 2: If the proposed road end point is "near" this road's
			// start or end points, deferred snap.

			// Measure distance of ending point of the path - to intersection
			// and to the whole path
			Point p1 = tempPath.end(), p2 = currentRoad.getFrom().position();
			distance = new Vector3d(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z).length();
			if (distance < snapDistance && distance < distanceToNearestIntersection) {
				isClose = true;
				if (checkSnapPossibility(tempPath, currentRoad.getFrom())) {
					nearestIntersection = currentRoad.getFrom();
					distanceToNearestIntersection = distance;
				}
			}

			p1 = tempPath.end();
			p2 = currentRoad.getTo().position();
			distance = new Vector3d(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z).length();
			if (distance < snapDistance && distance < distanceToNearestIntersection) {
				isClose = true;
				if (checkSnapPossibility(tempPath, currentRoad.getTo())) {
					nearestIntersection = currentRoad.getTo();
					distanceToNearestIntersection = distance;
				}
			}

			// Step 3: If the proposed road is "near" any point on this road,
			// deferred snap.

			p1 = tempPath.end();
			p2 = currentRoad.getPath().nearestPoint(tempPath.end()); // nearestPointOfRoad
			distance = new Vector3d(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z).length();
			if (distance < snapDistance && distance < distanceToNearestRoad) {
				isClose = true;
				if (checkSnapPossibility(tempPath, currentRoad)) {
					nearestRoad = currentRoad;
					distanceToNearestRoad = distance;
				}
			}

			// Step 4: this road is "near" both the start AND end points of the
			// proposed path. these two roads are too close together. Kill the
			// proposed path.

			// Measure similarity of the two paths
			// proposedPath is too close to some existing path
			if (tempPath.distance(currentRoad.getTo().position()) < snapDistance
					&& tempPath.distance(currentRoad.getFrom().position()) < snapDistance) {
				logger.trace("proposedPath is TOO CLOSE to an existing road.");
				return false;
			}

			// Some existing road is too close
			if (currentRoad.getPath().distance(tempPath.begining()) < snapDistance
					&& currentRoad.getPath().distance(tempPath.end()) < snapDistance) {
				logger.trace("proposedPath is TOO CLOSE to an existing road.");
				return false;
			}

		}

		if (nearestIntersection != null) {
			// we found an existing intersection that is "near" to one of this
			// road's endpoints. Snap it.

			logger.trace("Snapping proposedPath to an existing intersection.");
			snapped = true;
			tempPath.setEnd(nearestIntersection.position());

			/* Snap to intersection */

		} else {
			if (nearestRoad != null) {
				// we found an existing road that is "near" to one of this
				// road's endpoints. Snap it.
				logger.trace("Snapping proposedPath to a point on an existing road.");
				snapped = true;
				tempPath.setEnd(nearestRoad.getPath().nearestPoint(tempPath.end()));	//FIXME: This doesn't actually create a new intersection, update the adjacency list, or anything.
			}
		}

		if (isClose && !snapped) {
			// the road is close, but we didn't snap it, for some reason.
			logger.trace("proposedPath is close to an existing road or intersection, but couldn't be snapped for some reason.");
			return false;
		}

		if (tempPath.length() < MINIMAL_ROAD_LENGTH) {
			// the road is too short now.
			logger.trace("proposedPath is TOO SHORT.");
			return false;
		}

		if (targetStreetGraph.isIntersectionAtPosition(tempPath.end())
				&& targetStreetGraph.getIntersectionAtPosition(tempPath.end()).numberOfWays() >= 4) // FIXME:
																										// Magic
																										// number.
		{
			logger.trace("proposedPath ends at an intersection with more than three existing roads. Canceling");
			// there is an existing intersection at the proposed location, and
			// it's already too busy. (ie: has more than three roads
			// leading away from it)
			return false;
		}
		//
		// for (Road currentRoad : targetStreetGraph.getRoads())
		// {
		// // Check for intersection
		// IntersectionType intersectionType =
		// tempPath.crosses(currentRoad.getPath(), intersection);
		// switch (intersectionType) {
		// case INTERSECTING:
		// if (intersection == tempPath.begining() ||
		// intersection == tempPath.end())
		// /* New road is just touching some other one */
		// {
		// }
		// else
		// /* Cut off the end of the path. */
		// {
		// assert(false);
		// //tempPath.setEnd(intersection);
		// }
		// break;
		// case CONTAINED:
		// assert (false);
		// break;
		// case IDENTICAL:
		// assert (false);
		// break;
		// case CONTAINING:
		// assert (false);
		// break;
		// case OVERLAPPING:
		// assert (false);
		// break;
		// case ORTHOGONAL:
		// break;
		// case PARALLEL:
		// break;
		// default:
		// break;
		// }
		// }
		proposedPath.set(tempPath);
		return true;
	}

	protected void cancelBranch() {
		// Remove everything that would be drawn from this position
		while (currentlyInterpretedSymbol + 1 < producedString.size()) {
			if (producedString.get(currentlyInterpretedSymbol + 1).getSymbol() == ']')
				break;
			producedString.remove(currentlyInterpretedSymbol + 1);
		}
	}

	protected double getRoadSegmentLength() {
		return MathUtil.random.nextDouble() * (maxRoadLength - minRoadLength) + minRoadLength;
	}

	protected double getTurnAngle() {
		return MathUtil.random.nextDouble() * (maxTurnAngle - minTurnAngle) + minTurnAngle;
	}

	protected boolean checkSnapPossibility(Path proposedPath, Intersection intersection) {
		double distance = intersection.position().distance(proposedPath.end());
		if (distance < snapDistance) {
			if (targetStreetGraph.getIntersectionAtPosition(intersection.position()).numberOfWays() < 4) {
				/*
				 * Snap to intersection -- we need to check for intersections
				 * with adjacent roads of the intersection again with this
				 * particular road to avoid overlaps and stuff like that.
				 */
				Path snappedPath = new Path(proposedPath);
				snappedPath.setEnd(intersection.position());

				Set<Road> intersectionRoads = intersection.getRoads();
				IntersectionType intersectionType;
				Point intersectionPoint = new Point();
				for (Road adjacentRoad : intersectionRoads) {
					intersectionType = snappedPath.crosses(adjacentRoad.getPath(), intersectionPoint);
					if (intersectionType == IntersectionType.IDENTICAL || intersectionType == IntersectionType.CONTAINED
							|| intersectionType == IntersectionType.CONTAINING
							|| intersectionType == IntersectionType.OVERLAPPING) {
						/* Not good snap point */
						return false;
					}
				}
				/* Sound's good */
				return true;
			}
		}

		return false;
	}

	protected boolean checkSnapPossibility(Path proposedPath, Road road) {
		double distance = road.getPath().nearestPoint(proposedPath.end()).distance(proposedPath.end());
		if (distance < snapDistance) {
			if (road.getPath().begining().distance(road.getPath().end()) >= MINIMAL_ROAD_LENGTH && road.getPath()
					.nearestPoint(proposedPath.end()).distance(road.getPath().end()) >= MINIMAL_ROAD_LENGTH) {
				/*
				 * Snap to road -- we need to check intersections again with
				 * this particular road to avoid overlaps and stuff like that.
				 */
				Path snappedPath = new Path(proposedPath);
				snappedPath.setEnd(road.getPath().nearestPoint(proposedPath.end()));

				IntersectionType intersectionType;
				Point intersection = new Point();
				intersectionType = snappedPath.crosses(road.getPath(), intersection); // BYREF
																						// intersection
				if (intersectionType == IntersectionType.IDENTICAL || intersectionType == IntersectionType.CONTAINED
						|| intersectionType == IntersectionType.CONTAINING
						|| intersectionType == IntersectionType.OVERLAPPING) {
					/* Not good snap point */
					return false;
				} else /* Sounds good */
				{
					return true;
				}
			}
		}

		return false;
	}

}
