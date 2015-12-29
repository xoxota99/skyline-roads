package com.skyline.roadsys.geometry;

import javax.vecmath.*;

import com.skyline.roadsys.util.*;

/**
 * Extends Point3d, just so we can add our own equals() method that takes the
 * EPSILON value into account.
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */
public class Point extends Point3d implements Comparable<Point> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 318981590595472530L;

	public Point(Tuple3d coordinates) {
		super(coordinates);
	}

	public Point() {
		super();
	}

	public Point(double x, double y, double z) {
		super(x, y, z);
	}

	public Point(double x, double y) {
		this(x, y, 0);
	}

	public boolean equals(Tuple3d t) {
		// distance must be <= COORDINATES_EPSILON.
		double d2 = (t.x - this.x) * (t.x - this.x) + (t.y - this.y) * (t.y - this.y);
		return (d2 <= Units.COORDINATES_EPSILON * Units.COORDINATES_EPSILON);
	}

	@Override
	public int compareTo(Point other) {
		// This is the original implementation. Not particularly correct, but
		// matches the expectation set in AreaExtractor.addVertex
		if (x != other.x) {
			return other.x - x > 0 ? 1 : -1;
		} else if (y != other.y) {
			return other.y - y > 0 ? 1 : -1;
		}
		// Disregard Z.
		return 0;

	}

	public int compareTo2(Point other) {
		// NOTE: Modified from operator< and operator>. More correct, but may
		// introduce unexpected behavior.
		if (this.equals(other)) {
			return 0;
		} else {
			Point3d zero = new Point3d();
			return other.distance(zero) > this.distance(zero) ? -1 : 1;
		}
	}

	/*
	 * Because vecmath doesn't daisy-chain (for performance reasons,
	 * presumeably), we have to implement the same bloody thing, but properly.
	 */
	public Point plus(Tuple3d vector) {
		Point p = new Point(this);
		p.add(vector);
		return p;
	}
}
