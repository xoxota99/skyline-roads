package com.skyline.roadsys.geometry;

import org.junit.*;

import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.util.*;

public class TestLine {

	@Test
	public void testInterface()
	{
		Line line = new Line();

		line.setBegining(new Point(0, 0, 0));
		line.setEnd(new Point(0, 0, 0));

		Assert.assertTrue(new Point(0, 0, 0).equals(line.begining()));
		Assert.assertTrue(new Point(0, 0, 0).equals(line.end()));
	}

	@Test
	public void testHasPoint()
	{
		Line line = new Line(new Point(0, 0), new Point(1, 1));

		Assert.assertTrue(line.hasPoint2D(new Point(0, 0)));
		Assert.assertTrue(line.hasPoint2D(new Point(1, 1)));
		Assert.assertTrue(line.hasPoint2D(new Point(0.5, 0.5)));
		Assert.assertTrue(line.hasPoint2D(new Point(2, 2)));
		Assert.assertTrue(!line.hasPoint2D(new Point(3, 4)));

		line = new Line(new Point(1, 1), new Point(-1, 1));
		Assert.assertTrue(line.hasPoint2D(new Point(-0.5, 1)));
	}

	@Test
	public void testIntersecting()
	{
		Line l1 = new Line(new Point(0, 0), new Point(1, 1)), l2 = new Line(new Point(0, 1), new Point(1, 0));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.INTERSECTING, l1.intersection2D(l2, result));
		Assert.assertTrue(new Point(0.5, 0.5).equals(result));
	}

	@Test
	public void testParallel()
	{
		Line l1 = new Line(new Point(0, 0), new Point(1, 1)), l2 = new Line(new Point(1, 1), new Point(2, 2));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.PARALLEL, l1.intersection2D(l2, result));
	}

	@Test
	public void testIdentical()
	{
		Line l1 = new Line(new Point(0, 0), new Point(1, 1)), l2 = new Line(new Point(0, 0), new Point(1, 1));
		Point result = new Point();

		Assert.assertEquals(IntersectionType.PARALLEL, l1.intersection2D(l2, result));
	}

	@Test
	public void testPointDistance()
	{
		Line l = new Line(new Point(0, 0), new Point(10, 10));

		Assert.assertEquals(0, l.distance(new Point(-1, -1)), Units.EPSILON);
		Assert.assertEquals(0, l.distance(new Point(11, 11)), Units.EPSILON);
		Assert.assertEquals(Math.sqrt(2) / 2, l.distance(new Point(3, 4)), Units.EPSILON);
		Assert.assertEquals(Math.sqrt(2) / 2, l.distance(new Point(4, 3)), Units.EPSILON);
	}

}
