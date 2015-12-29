package com.skyline.roadsys.geometry;

import javax.vecmath.*;

import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.util.*;

/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */
public class LineSegment extends Line {

	public LineSegment(Point first, Point second) {
		super(first, second);
	}

	public LineSegment(Point point, Vector3d vector) {
		this(point, point.plus(vector));
	}

	/** < Own copy ructor is neccessary */
	public LineSegment(LineSegment source) {
		super(source);
	}

	public LineSegment() {
		// TODO Auto-generated constructor stub
	}

	public double length() {
		return first.distance(second);
	}

	/**
	 * Returns the "2D normal" of this LineSegment (normal of the projection
	 * onto the X/Y plane).
	 * 
	 * @return
	 */
	public Vector3d normal() {
		Vector3d dir = new Vector3d(second);
		dir.sub(first);
		dir.set(-dir.y, dir.x, 0);

		return dir;
	}

	public boolean hasPoint2D(Point point)
	{
		double lineTest = (point.x - first.x) * (second.y - first.y) -
				(point.y - first.y) * (second.x - first.x);
		// debug(Math.abs(lineTest));
		// debug(toString());
		// debug(point.toString());
		if (Math.abs(lineTest) < Units.COORDINATES_EPSILON)
		/* Point is on the line */
		{
			double t = 0.0;
			if (Math.abs(first.x - second.x) > Units.COORDINATES_EPSILON) // (first.x
																			// !=
																			// second.x)
			{
				t = (point.x - first.x) / (second.x - first.x);
				// debug(t);
				return t >= 0 && t <= 1;
			}
			else if (Math.abs(first.y - second.y) > Units.COORDINATES_EPSILON) // (first.y
																				// !=
																				// second.y)
			{
				t = (point.y - first.y) / (second.y - first.y);
				// debug(t);
				return t >= 0 && t <= 1;
			}
			else
			{
				return first.equals(point);
			}
		}

		return false;
	}

	public IntersectionType intersection2D(Line another, Point intersection) {
		double r, d; // r, s, d

		double x1 = begining().x, y1 = begining().y, x2 = end().x, y2 = end().y, x3 = another.begining().x, y3 = another.begining().y, x4 = another.end().x, y4 = another.end().y;

		// Make sure the lines aren't parallel
		if (Math.abs((y2 - y1) / (x2 - x1) - (y4 - y3) / (x4 - x3)) > Units.EPSILON)
		{
			d = (((x2 - x1) * (y4 - y3)) - (y2 - y1) * (x4 - x3));
			if (d != 0)
			{
				r = (((y1 - y3) * (x4 - x3)) - (x1 - x3) * (y4 - y3)) / d;
				// s = (((y1 - y3) * (x2 - x1)) - (x1 - x3) * (y2 - y1)) / d;
				if (r >= -Units.EPSILON && r <= (1 + Units.EPSILON))
				{
					intersection.set(x1 + r * (x2 - x1), y1 + r * (y2 - y1), 0);
					return IntersectionType.INTERSECTING;
				}
			}
		}

		return IntersectionType.ORTHOGONAL;
	}

	public double distance(Point point) {
		Point closestPoint = nearestPoint(point);
		Vector3d v = new Vector3d(point);
		v.sub(closestPoint);
		return v.length();
	}

	public Point nearestPoint(Point point) {
		double parameter;
		Point orthogonalProjection = new Point();

		parameter = (second.x - first.x) * (point.x - first.x) +
				(second.y - first.y) * (point.y - first.y) +
				(second.z - first.z) * (point.z - first.z);

		parameter /= length() * length();

		if (parameter >= 0 && parameter <= 1)
		{
			orthogonalProjection.x = ((1 - parameter) * first.x + second.x * parameter);
			orthogonalProjection.y = ((1 - parameter) * first.y + second.y * parameter);
			orthogonalProjection.z = ((1 - parameter) * first.z + second.z * parameter);

			return orthogonalProjection;
		}
		else
		{
			Vector3d v = new Vector3d(first);
			v.sub(point);
			double distanceToFirst = v.length();

			v.set(second);
			v.sub(point);

			double distanceToSecond = v.length();

			if (distanceToFirst < distanceToSecond)
			{
				return first;
			}
			else
			{
				return second;
			}
		}
	}

	public String toString() {
		return "LineSegment(" + first.toString() + ", " + second.toString() + ")";
	}

	public boolean equals(LineSegment another) {
		return (begining().equals(another.begining()) && end().equals(another.end())) ||
				(begining().equals(another.end()) && end().equals(another.begining()));
	}

	/**
	 * Test whether this Line Segment crosses the given Segment.
	 * 
	 * @param another
	 *            the segment to test against this segment.
	 * @param intersection
	 *            If the segments cross, the intersection point will be returned
	 *            in this output param.
	 * @return an {@link IntersectionType}, indicating the type of intersection,
	 *         if any.
	 */
	// TODO: Refactor this. IntersectionType should be a class containing
	// IntersectionType (OVERLAPPING, INTERSECTING, etc) and intersectionPoint.
	// Then remove the BYREF / output param. Output params are an anti-pattern
	// in Java.
	public IntersectionType intersection2D(LineSegment another, Point intersection) {
		// BYREF:
		// intersection

		// NOTE: intersection can be null. (See RoadLSystem.localraints)

		// SOURCE: http://paulbourke.net/geometry/lineline2d/
		double denominator = ((another.end().y - another.begining().y) * (end().x - begining().x)) -
				((another.end().x - another.begining().x) * (end().y - begining().y)), firstNumerator = ((another.end().x - another.begining().x) * (begining().y - another.begining().y)) -
				((another.end().y - another.begining().y) * (begining().x - another.begining().x)), secondNumerator = ((end().x - begining().x) * (begining().y - another.begining().y)) -
				((end().y - begining().y) * (begining().x - another.begining().x));

		if (Math.abs(denominator) < Units.COORDINATES_EPSILON)
		/* If the denominator is 0, both lines have same direction vector */
		{

			if (Math.abs(firstNumerator) < Units.COORDINATES_EPSILON &&
					Math.abs(secondNumerator) < Units.COORDINATES_EPSILON)
			/* Lines are coincident. */
			{

				/*
				 * WARNING Order of following checks is important for the right
				 * functionality.
				 */
				if (this == another || this.equals(another))
				/* Line segments are identical */
				{

					return IntersectionType.IDENTICAL;
				}

				if (hasPoint2D(another.begining()) && hasPoint2D(another.end()))
				/* This line is containing another. */
				{

					return IntersectionType.CONTAINING;
				}

				if (another.hasPoint2D(begining()) && another.hasPoint2D(end()))
				/* This line is contained in another. */
				{

					return IntersectionType.CONTAINED;
				}

				if (!this.hasPoint2D(another.begining()) &&
						!this.hasPoint2D(another.end()))
				/* Line segments are subsequent. */
				{

					return IntersectionType.ORTHOGONAL;
				}

				if (begining().equals(another.begining()) ||
						begining().equals(another.end()))
				/* Line segments touch just in one point. */
				{

					intersection.set(begining());
					return IntersectionType.INTERSECTING;
				}

				if (end().equals(another.end()) ||
						end().equals(another.begining()))
				/* Line segments touch just in one point. */
				{

					intersection.set(end());
					return IntersectionType.INTERSECTING;
				}

				/* Line segments overlap */
				return IntersectionType.OVERLAPPING;
			}

			// Lines are parallel thus nonintersecting
			return IntersectionType.ORTHOGONAL;
		}

		double ua = firstNumerator / denominator, ub = secondNumerator / denominator;

		if (ua >= 0 && ua <= 1 &&
				ub >= 0 && ub <= 1)
		{

			intersection.x = (begining().x + ua * (end().x - begining().x));
			intersection.y = (begining().y + ua * (end().y - begining().y));

			return IntersectionType.INTERSECTING;
		}

		return IntersectionType.ORTHOGONAL;
	}

}
