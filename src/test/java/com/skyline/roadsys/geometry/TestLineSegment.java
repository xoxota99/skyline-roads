package com.skyline.roadsys.geometry;

import org.junit.*;

import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.util.*;

public class TestLineSegment {

	@Test
	public void testInterface()
	{
		LineSegment line = new LineSegment();

		line.setBeginning(new Point(0, 0, 0));
		line.setEnd(new Point(0, 0, 0));
	}

	@Test
	public void testHasPoint()
	{
		LineSegment line = new LineSegment(new Point(0, 0), new Point(1, 1));

		Assert.assertTrue(line.hasPoint2D(new Point(0, 0)));
		Assert.assertTrue(line.hasPoint2D(new Point(1, 1)));
		Assert.assertTrue(line.hasPoint2D(new Point(0.5, 0.5)));
		Assert.assertTrue(!line.hasPoint2D(new Point(2, 2)));

		line = new LineSegment(new Point(1, 1), new Point(-1, 1));
		Assert.assertTrue(line.hasPoint2D(new Point(-0.5, 1)));
	}

	@Test
	public void testHasPoint2()
	{
		LineSegment line = new LineSegment(new Point(1, 1), new Point(0, 0));

		Assert.assertTrue(line.hasPoint2D(new Point(0, 0)));
		Assert.assertTrue(line.hasPoint2D(new Point(1, 1)));
		Assert.assertTrue(line.hasPoint2D(new Point(0.5, 0.5)));
		Assert.assertTrue(!line.hasPoint2D(new Point(2, 2)));

		line = new LineSegment(new Point(1, 1), new Point(-1, 1));
		Assert.assertTrue(line.hasPoint2D(new Point(-0.5, 1)));
	}

	@Test
	public void testIntersecting()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1)), l2 = new LineSegment(new Point(0, 1), new Point(1, 0));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.INTERSECTING, l1.intersection2D(l2, result));
		Assert.assertTrue(new Point(0.5, 0.5).equals(result));
	}

	@Test
	public void testIntersecting2()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1));
		LineSegment l2 = new LineSegment(new Point(1, 1), new Point(2, 2));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.INTERSECTING, l1.intersection2D(l2, result));
		Assert.assertTrue(new Point(1, 1).equals(result));
	}

	@Test
	public void testIntersecting3()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1)), l2 = new LineSegment(new Point(1, 1), new Point(0, 5));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.INTERSECTING, l1.intersection2D(l2, result));
		Assert.assertTrue(new Point(1, 1).equals(result));
	}

	@Test
	public void testNonintersecting()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1)), l2 = new LineSegment(new Point(2, 3), new Point(-3, 0));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.ORTHOGONAL, l1.intersection2D(l2, result));
	}

	@Test
	public void testParallel()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1)), l2 = new LineSegment(new Point(0, 1), new Point(1, 2));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.ORTHOGONAL, l1.intersection2D(l2, result));
	}

	@Test
	public void testCoincident1()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1)), l2 = new LineSegment(new Point(0, 0), new Point(0.5, 0.5));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.CONTAINING, l1.intersection2D(l2, result));
	}

	@Test
	public void testCoincident2()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(0.5, 0.5)), l2 = new LineSegment(new Point(0, 0), new Point(1, 1));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.CONTAINED, l1.intersection2D(l2, result));
	}

	@Test
	public void testCoincident3()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1)), l2 = new LineSegment(new Point(0, 0), new Point(1, 1));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.IDENTICAL, l1.intersection2D(l2, result));
	}

	@Test
	public void testCoincident4()
	{
		LineSegment l1 = new LineSegment(new Point(0, 0), new Point(1, 1)), l2 = new LineSegment(new Point(0.5, 0.5), new Point(1.5, 1.5));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.OVERLAPPING, l1.intersection2D(l2, result));
	}

	// public void testTrimByPolygon()
	// {
	// LineSegment line = new Line;
	// Polygon *polygon = new Polygon();
	//
	// polygon.addVertex(new Point(1,1));
	// polygon.addVertex(new Point(-1,1));
	// polygon.addVertex(new Point(-1,-1));
	// polygon.addVertex(new Point(1,-1));
	//
	// line = Line(new Point(0,0), new Point(10,10));
	// line.trimOverlapingPart(*polygon);
	// Assert.assertTrue(line == LineSegment(new Point(0,0), new Point(1,1)));
	//
	// line = LineSegment(new Point(0,0), new Point(0,10));
	// line.trimOverlapingPart(*polygon);
	// Assert.assertTrue(line == LineSegment(new Point(0,0), new Point(0,1)));
	//
	// line = LineSegment(new Point(0,1), new Point(10,1));
	// line.trimOverlapingPart(*polygon);
	// Assert.assertTrue(line == LineSegment(new Point(0,1), new Point(1,1)));
	//
	// delete line;
	// delete polygon;
	// }

	@Test
	public void testWeirdCase()
	{
		LineSegment l1 = new LineSegment(new Point(57.8629, 218.793, 0), new Point(77.2648, 197.245, 0)), l2 = new LineSegment(new Point(57.8629, 218.793, 0), new Point(77.2648, 197.245, 0));

		Point intersection = new Point();

		Assert.assertEquals(IntersectionType.IDENTICAL, l1.intersection2D(l2, intersection));
	}

	@Test
	public void testPointDistance()
	{
		LineSegment l = new LineSegment(new Point(0, 0), new Point(10, 10));

		Assert.assertEquals(Math.sqrt(2), l.distance(new Point(-1, -1)), Units.EPSILON);
		Assert.assertEquals(Math.sqrt(2), l.distance(new Point(11, 11)), Units.EPSILON);
		Assert.assertEquals(Math.sqrt(2) / 2, l.distance(new Point(3, 4)), Units.EPSILON);
		Assert.assertEquals(Math.sqrt(2) / 2, l.distance(new Point(4, 3)), Units.EPSILON);
	}

	@Test
	public void testContained()
	{
		LineSegment a = new LineSegment(new Point(-3000, -2509.3, 0), new Point(-3000, -2244.59, 0));
		LineSegment b = new LineSegment(new Point(-3000, 837.305, 0), new Point(-3000, -2509.3, 0));

		IntersectionType result;
		Point point = new Point();
		result = a.intersection2D(b, point);

		Assert.assertTrue(IntersectionType.CONTAINED == result);

		result = b.intersection2D(a, point);
		Assert.assertTrue(IntersectionType.CONTAINING == result);
	}

}
