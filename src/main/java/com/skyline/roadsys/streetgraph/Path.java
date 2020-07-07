package com.skyline.roadsys.streetgraph;

import javax.vecmath.*;

import com.skyline.roadsys.geometry.*;

/**
 * Path representation
 * 
 * Says what path takes the road between two intersections.
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class Path {

	private LineSegment representation;

	public Path(double x1,double y1,double x2,double y2){
		Point p1 = new Point(x1,y1);
		Point p2 = new Point(x2,y2);
		representation = new LineSegment(p1,p2);
	}
	public Path() {
		representation = new LineSegment();
	}

	public Path(LineSegment line) {
		representation = new LineSegment(line);
	}

	public Path(Path source) {
		representation = new LineSegment(source.representation);
	}

	public Point beginning() {
		return representation.beginning();
	}

	public Point end() {
		return representation.end();
	}

	/**
	 * Test whether this path crosses the given path.
	 * 
	 * @param anotherPath
	 *            the path to test against this path.
	 * @param intersection
	 *            If the paths cross, the intersection point will be returned in
	 *            this output param.
	 * @return an {@link IntersectionType}, indicating the type of
	 *         intersection, if any.
	 */
	public IntersectionType crosses(Path anotherPath, Point intersection) { // BYREF:
		// intersection
		return representation.intersection2D(anotherPath.representation, intersection);
	}

	public Point nearestPoint(Point point)
	{
		return representation.nearestPoint(point);
	}

	public void shorten(Point newBegining, Point newEnd)
	{
		if (!representation.hasPoint2D(newBegining) ||
				!representation.hasPoint2D(newEnd))
		{
			// FIXME: throw an exception
			throw new IllegalArgumentException("Tried to shorten a segment, but the new endpoint(s) are not on the segment! Existing segment: " + representation.toString() + ", newBeginning: " + newBegining + ", newEnd: " + newEnd);
		}

		representation.setBegining(newBegining);
		representation.setEnd(newEnd); // WTF: Was "setBeginning(newEnd)"
										// originally.
	}

	public String toString() {
		return "Path(" + representation.toString() + ")";
	}

	public double length() {
		return representation.length();
	}

	/**
	 * Extract vector that what direction would this path take if it should be
	 * expanded beyond the beginning point.
	 * 
	 * The direction is opposite to the direction of the path. Path goes from
	 * beginning to end, this vector goes away from beginning (or backwards).
	 * 
	 * @return Normalized direction Vector.
	 */
	public Vector3d beginingDirectionVector()
	{
		Vector3d beginingDirection = new Vector3d(begining());
		beginingDirection.sub(end());
		beginingDirection.normalize();

		return beginingDirection;
	}

	public void setEnd(Point end) {
		representation.setEnd(end);
	}

	public void setBegining(Point begining) {
		representation.setBegining(begining);
	}

	/**
	 * Extract vector that what direction would this path take if it should be
	 * expanded beyond the end point.
	 * 
	 * The direction is same to the direction of the path. Path goes from
	 * begining to end.
	 * 
	 * @return Normalized direction Vector.
	 */
	public Vector3d endDirectionVector() {
		Vector3d endDirection = new Vector3d(end());
		endDirection.sub(begining());
		endDirection.normalize();

		return endDirection;
	}

	/**
	 * Is at least one end of this Path contained within the given Polygon?
	 * 
	 * @param certainArea
	 *            the polygon to check
	 * @return true if at least one end of this path is inside the polygon,
	 *         otherwise false.
	 */
	public boolean isInside(Polygon certainArea) {
		return certainArea.encloses2D(begining()) ||
				certainArea.encloses2D(end());
	}

	public boolean goesThrough(Point position) {
		return representation.hasPoint2D(position);
	}

	public double distance(Point point) {
		return representation.distance(point);
	}
	public void set(Path path) {
		this.representation.first.set(path.representation.first);
		this.representation.second.set(path.representation.second);
	}
}
