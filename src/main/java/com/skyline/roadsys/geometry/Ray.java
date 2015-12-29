package com.skyline.roadsys.geometry;

import javax.vecmath.*;

import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.util.*;

/**
 * Representation of a ray
 * 
 * Also basic geometric operation with line are implemented here.
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class Ray {
	private Point rayOrigin;
	private Vector3d rayDirection;

	public Ray() {
		initialize();
		rayOrigin.set(0, 0, 0);
		rayDirection.set(1, 0, 0);
	}

	public Ray(Point point, Vector3d vector) {
		initialize();
		rayOrigin = point;
		rayDirection = vector;
	}

	public Ray(Point firstPoint, Point secondPoint) {
		initialize();
		rayOrigin = firstPoint;
		rayDirection = new Vector3d(secondPoint);
		rayDirection.sub(firstPoint);
	}

	public Ray(Ray source) {
		initialize();
		rayOrigin = source.origin();
		rayDirection = source.direction();
	}

	public void set(Point point, Vector3d vector) {
		rayOrigin = point;
		rayDirection = vector;
	}

	public void setOrigin(Point point) {
		rayOrigin = point;
	}

	public void setDirection(Vector3d vector) {
		rayDirection = vector;
	}

	public Point origin() {
		return rayOrigin;
	}

	public Vector3d direction() {
		return rayDirection;
	}

	public IntersectionType intersection2D(Ray another, Point intersection) // BYREF
																			// intersection

	/*
	 * Algorithm adapted from http://pastebin.com/f22ec3cf1
	 * http://www.gamedev.net/topic/518648-intersection-of-rays/
	 */
	{
		double r, s, d;

		double x1 = origin().x, y1 = origin().y, x2 = origin().x + direction().x, y2 = origin().y + direction().y, x3 = another.origin().x, y3 = another.origin().y, x4 = another.origin().x + another.direction().x, y4 = another.origin().y + another.direction().y;

		if (Math.abs((y2 - y1) / (x2 - x1) - (y4 - y3) / (x4 - x3)) > Units.EPSILON) // should
																						// be
																						// COORDINATES_EPSILON
																						// ?
		/* Make sure the lines aren't parallel */
		{
			d = (((x2 - x1) * (y4 - y3)) - (y2 - y1) * (x4 - x3));
			if (d != 0)
			{
				r = (((y1 - y3) * (x4 - x3)) - (x1 - x3) * (y4 - y3)) / d;
				s = (((y1 - y3) * (x2 - x1)) - (x1 - x3) * (y2 - y1)) / d;
				if (r >= 0 || r >= -Units.EPSILON) // should be
													// COORDINATES_EPSILON ?
				{
					if (s >= 0 || s >= -Units.EPSILON) // should be
														// COORDINATES_EPSILON ?
					{
						intersection.set(x1 + r * (x2 - x1), y1 + r * (y2 - y1), 0); // BYREF
																						// intersection.
						return IntersectionType.INTERSECTING;
					}
				}
			}
		}
		else
		{
			return IntersectionType.PARALLEL;
		}
		return IntersectionType.ORTHOGONAL;
	}

	/**
	 * Find the 2-dimensional intersection (X/Y) point between a Ray and a Line, if one exists.
	 * Return the Intersection Type (one of PARALLEL, INTERSECTING, or
	 * ORTHOGONAL).
	 * 
	 * @param - The Line to test for intersection
	 * @param intersection
	 *            - output param containing the Point where the Line intersects
	 *            this Ray, if any.
	 * @return
	 */
	public IntersectionType intersection2D(Line line, Point intersection) // BYREF
																			// intersection

	/*
	 * Algorithm adapted from http://pastebin.com/f22ec3cf1
	 * http://www.gamedev.net/topic/518648-intersection-of-rays/
	 */
	{
		double r, d;

		double x1 = origin().x;
		double y1 = origin().y;
		double x2 = origin().x + direction().x;
		double y2 = origin().y + direction().y;

		double x3 = line.begining().x;
		double y3 = line.begining().y;
		double x4 = line.end().x;
		double y4 = line.end().y;

		if (((x2 - x1) == 0 && (x4 - x3) == 0) ||
				((y2 - y1) == 0 && (y4 - y3) == 0))
		{
			return IntersectionType.PARALLEL;
		}

		/* Make sure the lines aren't parallel. Compare slopes. */
		if (Math.abs((y2 - y1) / (x2 - x1) - (y4 - y3) / (x4 - x3)) > Units.EPSILON)
		{
			d = (((x2 - x1) * (y4 - y3)) - (y2 - y1) * (x4 - x3)); // FIXME: <--
																	// Bug Here?
			if (d != 0) {
				r = (((y1 - y3) * (x4 - x3)) - (x1 - x3) * (y4 - y3)) / d;
				// s = (((y1 - y3) * (x2 - x1)) - (x1 - x3) * (y2 - y1)) / d;
				if (r >= 0 || r >= -Units.EPSILON) // Check with an EPSILON to
													// count in double error
				{ // WTF: s is missing? (see intersection2D(Ray)
					intersection.set(x1 + r * (x2 - x1), y1 + r * (y2 - y1), 0);
					return IntersectionType.INTERSECTING;
				}
			}
		}
		else
		{
			return IntersectionType.PARALLEL;
		}
		return IntersectionType.ORTHOGONAL;
	}

	public IntersectionType intersection2D(LineSegment another, Point intersection) // BYREF
																					// intersection
	/*
	 * Algorithm adapted from http://pastebin.com/f22ec3cf1
	 * http://www.gamedev.net/topic/518648-intersection-of-rays/
	 */
	{
		double r, s, d;

		double x1 = origin().x, y1 = origin().y, x2 = origin().x + direction().x, y2 = origin().y + direction().y, x3 = another.begining().x, y3 = another.begining().y, x4 = another.end().x, y4 = another.end().y;

		if (Math.abs((y2 - y1) / (x2 - x1) - (y4 - y3) / (x4 - x3)) > Units.COORDINATES_EPSILON)
		/* Make sure the lines aren't parallel */
		{
			d = (((x2 - x1) * (y4 - y3)) - (y2 - y1) * (x4 - x3));
			if (d != 0)
			{
				r = (((y1 - y3) * (x4 - x3)) - (x1 - x3) * (y4 - y3)) / d;
				s = (((y1 - y3) * (x2 - x1)) - (x1 - x3) * (y2 - y1)) / d;
				if (r >= 0 || r >= -Units.COORDINATES_EPSILON) // Check with an
																// EPSILON to
																// count in
																// double error
				{
					if ((s >= 0 || s >= -Units.COORDINATES_EPSILON) &&
							(s <= 1 || Math.abs(s - 1) <= Units.COORDINATES_EPSILON))
					{
						intersection.set(x1 + r * (x2 - x1), y1 + r * (y2 - y1), 0);
						return IntersectionType.INTERSECTING;
					}
				}
			}
		}
		else
		{
			return IntersectionType.PARALLEL;
		}
		return IntersectionType.ORTHOGONAL;
	}

	public String toString() {
		return "Ray(" + rayOrigin.toString() + ", " + rayDirection.toString() + ")";
	}

	private void initialize()
	{
		rayOrigin = new Point();
		rayDirection = new Vector3d();
	}
}
