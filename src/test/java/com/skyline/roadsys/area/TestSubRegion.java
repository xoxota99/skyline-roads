package com.skyline.roadsys.area;

import org.junit.*;

import com.skyline.roadsys.geometry.*;

public class TestSubRegion {

	@Test
	public void testInitializeFromPolygon()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		SubRegion r = new SubRegion(p);
		SubRegion.Edge e = r.getFirstEdge();

		Assert.assertTrue(new Point(0, 0).equals(e.beginning));
		Assert.assertTrue(new Point(10, 0).equals(e.next.beginning));
		Assert.assertTrue(new Point(0, 10).equals(e.previous.beginning));
	}

	@Test
	public void testInsertInitialize()
	{
		SubRegion r = new SubRegion();
		SubRegion.Edge e;

		SubRegion.Edge current = r.insert(null, new Point(0, 0));
		current = r.insert(current, new Point(10, 0));
		current = r.insert(current, new Point(10, 10));
		current = r.insert(current, new Point(0, 10));

		e = r.getFirstEdge();
		Assert.assertTrue(new Point(0, 0).equals(e.beginning));
		Assert.assertTrue(new Point(10, 0).equals(e.next.beginning));
		Assert.assertTrue(new Point(0, 10).equals(e.previous.beginning));
		Assert.assertTrue(new Point(0, 0).equals(e.previous.next.beginning));
		Assert.assertTrue(new Point(10, 10).equals(e.previous.previous.beginning));
	}

	@Test
	public void testLongestEdges()
	{
		SubRegion r = new SubRegion();
		SubRegion.Edge current = r.insert(null, new Point(0, 0));
		current = r.insert(current, new Point(10, 0));
		current.hasRoadAccess = true;
		current = r.insert(current, new Point(10, 10));
		current.hasRoadAccess = false;

		Assert.assertTrue(new Point(10, 0).equals(r.getLongestEdgeWithRoadAccess().beginning));
		Assert.assertTrue(new Point(10, 10).equals(r.getLongestEdgeWithoutRoadAccess().beginning));

		Assert.assertTrue(r.hasRoadAccess());
	}

	@Test
	public void testToPolygon()
	{
		SubRegion r = new SubRegion();
		Polygon p = new Polygon();

		SubRegion.Edge current = r.insert(null, new Point(0, 0));
		current = r.insert(current, new Point(10, 0));
		current = r.insert(current, new Point(10, 10));
		current = r.insert(current, new Point(0, 10));

		p = r.toPolygon();
		Assert.assertTrue(new Point(0, 0).equals(p.vertex(0)));
		Assert.assertTrue(new Point(10, 0).equals(p.vertex(1)));
		Assert.assertTrue(new Point(10, 10).equals(p.vertex(2)));
		Assert.assertTrue(new Point(0, 10).equals(p.vertex(3)));
	}

	@Test
	public void testCopyConstructor()
	{
		SubRegion r = new SubRegion();
		SubRegion.Edge current = r.insert(null, new Point(0, 0));
		current = r.insert(current, new Point(10, 0));
		current = r.insert(current, new Point(10, 10));
		current = r.insert(current, new Point(0, 10));

		SubRegion s = new SubRegion(r);
		Assert.assertTrue(s.getFirstEdge().beginning.equals(r.getFirstEdge().beginning));
	}
}
