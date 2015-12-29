package com.skyline.roadsys.geometry;

import javax.vecmath.*;

import org.junit.*;

import com.skyline.roadsys.streetgraph.*;

public class TestRay {

	@Test
	public void testConstructors()
	{
		Ray r1 = new Ray();
		Assert.assertTrue(new Point(0, 0).equals(r1.origin()));
		Assert.assertTrue(new Vector3d(1, 0, 0).equals(r1.direction()));

		Ray r2 = new Ray(new Point(1, 1), new Vector3d(1, 1, 0));
		Assert.assertTrue(new Point(1, 1).equals(r2.origin()));
		Assert.assertTrue(new Vector3d(1, 1, 0).equals(r2.direction()));

		Ray r3 = new Ray(r2);
		Assert.assertTrue(new Point(1, 1).equals(r3.origin()));
		Assert.assertTrue(new Vector3d(1, 1, 0).equals(r3.direction()));
	}

	@Test
	public void testSetMethods()
	{
		Ray r = new Ray();

		r.setOrigin(new Point(10, 10));
		Assert.assertTrue(new Point(10, 10).equals(r.origin()));

		r.setDirection(new Vector3d(10, 10, 0));
		Assert.assertTrue(new Vector3d(10, 10, 0).equals(r.direction()));

		r.set(new Point(2, 3), new Vector3d(4, 5, 0));
		Assert.assertTrue(new Point(2, 3).equals(r.origin()));
		Assert.assertTrue(new Vector3d(4, 5, 0).equals(r.direction()));
	}

	@Test
	public void testIntersectionOfTwoRays()
	{
		Ray r1 = new Ray(), r2 = new Ray();
		Point intersection = new Point();

		// Intersecting
		r1.set(new Point(0, 0), new Vector3d(1, 0, 0));
		r2.set(new Point(1, 1), new Vector3d(0, -1, 0));
		Assert.assertEquals(IntersectionType.INTERSECTING, r1.intersection2D(r2, intersection));
		Assert.assertTrue(new Point(1, 0).equals(intersection));

		// Non intersecting
		r1.set(new Point(0, 0), new Vector3d(1, 0, 0));
		r2.set(new Point(1, 1), new Vector3d(0, 1, 0));
		Assert.assertEquals(IntersectionType.ORTHOGONAL, r1.intersection2D(r2, intersection));

		// Paralel
		r1.set(new Point(0, 0), new Vector3d(1, 1, 0));
		r2.set(new Point(1, 0), new Vector3d(1, 1, 0));
		Assert.assertEquals(IntersectionType.PARALLEL, r1.intersection2D(r2, intersection));

		// Touching
		r1.set(new Point(-16.3961, 20, 0), new Vector3d(-7.84465, -39.2232, 0));
		r2.set(new Point(-100, 20, 0), new Vector3d(1, 0, 0));
		Assert.assertEquals(IntersectionType.INTERSECTING, r2.intersection2D(r1, intersection));
	}

	@Test
	public void testLineRayIntersecting()
	{
		Ray r = new Ray();
		Line l = new Line();
		Point intersection = new Point();

		r.set(new Point(0, 0), new Vector3d(1, 0, 0));
		l.set(new Point(1, 1), new Point(1, -1));
		Assert.assertEquals(IntersectionType.INTERSECTING, r.intersection2D(l, intersection));
		Assert.assertTrue(new Point(1, 0).equals(intersection));
	}

	@Test
	public void testLineRayNonintersecting()
	{
		Ray r = new Ray();
		Line l = new Line();
		Point intersection = new Point();

		r.set(new Point(0, 0), new Vector3d(1, 0, 0));
		l.set(new Point(-1, 1), new Point(-1, -1));
		Assert.assertEquals(IntersectionType.ORTHOGONAL, r.intersection2D(l, intersection));
	}

	@Test
	public void testLineRayParallel()
	{
		Ray r = new Ray();
		Line l = new Line();
		Point intersection = new Point();

		r.set(new Point(0, 0), new Vector3d(1, 0, 0));
		l.set(new Point(-1, 1), new Point(1, 1));
		Assert.assertEquals(IntersectionType.PARALLEL, r.intersection2D(l, intersection));
	}

	@Test
	public void testLineRayCase()
	{
		Ray r = new Ray();
		Line l = new Line();
		Point intersection = new Point();

		r.set(new Point(3020, 3000, 0), new Vector3d(0, -1, 0));
		l.set(new Point(3020, 1876.1, 0), new Point(3020, 1916.18, 0));
		Assert.assertEquals(IntersectionType.PARALLEL, r.intersection2D(l, intersection));
	}

	@Test
	public void testRayToLineSegment()
	{
		Ray r = new Ray();
		Line l = new Line();
		Point intersection = new Point();

		r.set(new Point(0, 0), new Vector3d(1, 0, 0)); // Ray along positive X
		l.set(new Point(-1, 1), new Point(1, 1));
		Assert.assertEquals(IntersectionType.PARALLEL, r.intersection2D(l, intersection));

		r.set(new Point(0, 0), new Vector3d(0, 1, 0)); // Ray along positive Y
		l.set(new Point(-0.5, 0.5), new Point(0.5, 0.5));
		Assert.assertEquals(IntersectionType.INTERSECTING, r.intersection2D(l, intersection));

		r.set(new Point(0, 0), new Vector3d(1, 0, 0)); // Ray along positive X
		l.set(new Point(-1, 0), new Point(-1, -1));
		Assert.assertEquals(IntersectionType.ORTHOGONAL, r.intersection2D(l, intersection));
	}
}
