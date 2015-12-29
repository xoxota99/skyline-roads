package com.skyline.roadsys.geometry;

/**
 * Representation of an extruded Shape by a polygon base and the object height.
 * @author philippd
 *
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

public class Shape {

	private Polygon shapeBase;
	private double shapeHeight;

	public Shape() {
		initialize();
	}

//	public Shape(Polygon base, double height) {
//	}

	public Shape(Shape source) {
		initialize();

		shapeBase = new Polygon(source.shapeBase);
		shapeHeight = source.shapeHeight;
	}

	public Polygon base() {
		return shapeBase;
	}

	public Polygon top() {
		Polygon upperBase = new Polygon();
		Point p = new Point();
		for (int i = 0; i < shapeBase.numberOfVertices(); i++)
		{
			p.set(0, 0, 1);
			p.scale(shapeHeight);
			p.add(shapeBase.vertex(i));

			upperBase.addVertex(p);
		}

		return upperBase;
	}

	public double height() {
		return shapeHeight;
	}

	public void setBase(Polygon polygon) {
		shapeBase = polygon;
	}

	public void setHeight(double number) {
		shapeHeight = number;
	}

	public boolean encloses(Point point)
	{
		assert (shapeBase.numberOfVertices() > 0);

		double lowerZBound = shapeBase.vertex(0).z, higherZBound = lowerZBound + shapeHeight;

		return shapeBase.encloses2D(point) && point.z >= lowerZBound && point.z <= higherZBound;
	}

	public boolean encloses(Shape shape)
	{
		Polygon otherBase = shape.base();
		Point p = new Point();
		for (int i = 0; i < otherBase.numberOfVertices(); i++)
		{
			p.set(0, 0, 1);
			p.scale(shape.height());
			p.add(otherBase.vertex(i));
			if (!encloses(otherBase.vertex(i)) || !encloses(p))
			{
				return false;
			}
		}

		return true;
	}

	public boolean encloses(Polygon polygon)
	{
		for (int i = 0; i < polygon.numberOfVertices(); i++)
		{
			if (!encloses(polygon.vertex(i)))
			{
				return false;
			}
		}

		return true;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("Shape(" + shapeBase.toString());
		sb.append(", height = " + shapeHeight + ")");

		return sb.toString();
	}

	private void initialize() {
		shapeBase = new Polygon();
		shapeHeight = 0;
	}
}
