package com.skyline.roadsys.geometry;

import javax.vecmath.*;

import org.junit.*;

import com.skyline.roadsys.util.*;

public class TestPoint {
	@Test
	public void testAccessFunctions()
	{
		Point point = new Point();

		Assert.assertEquals(0, point.x, Units.EPSILON);
		Assert.assertEquals(0, point.y, Units.EPSILON);
		Assert.assertEquals(0, point.z, Units.EPSILON);

		point.set(1, 2, 3);

		Assert.assertEquals(1, point.x, Units.EPSILON);
		Assert.assertEquals(2, point.y, Units.EPSILON);
		Assert.assertEquals(3, point.z, Units.EPSILON);

		point.set(-1, -7, 0);
		Assert.assertEquals(-1, point.x, Units.EPSILON);
		Assert.assertEquals(-7, point.y, Units.EPSILON);
		Assert.assertEquals(0, point.z, Units.EPSILON);

		point.set(5, 6.3, 8.2);
		Assert.assertEquals(5, point.x, Units.EPSILON);
		Assert.assertEquals(6.3, point.y, Units.EPSILON);
		Assert.assertEquals(8.2, point.z, Units.EPSILON);

		point.set(1, 2, 3);
		Assert.assertEquals(1, point.x, Units.EPSILON);
		Assert.assertEquals(2, point.y, Units.EPSILON);
		Assert.assertEquals(3, point.z, Units.EPSILON);
	}

	@Test
	public void testOperators()
	{
		Point p1 = new Point(), p2 = new Point();
		Assert.assertEquals(p1, p2);

		p1.set(1.3, 2.2, 3.1);
		p2.set(1.3, 2.2, 3.1);
		Assert.assertEquals(p1, p2);
	}

	@Test
	public void testMinusOperator()
	{
		Point a = new Point(1, 0);
		Point b = new Point(2, 0);

		// testing b-a
		Vector3d ab = new Vector3d(b);
		ab.sub(a);

		Assert.assertEquals(new Vector3d(1, 0, 0), ab);
	}
}
