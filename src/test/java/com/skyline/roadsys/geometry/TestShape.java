package com.skyline.roadsys.geometry;

import org.junit.*;

public class TestShape {
	@Test
	public void testInterface()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Shape s = new Shape();
		s.setBase(p);
		s.setHeight(10);

		Assert.assertTrue(s.base().vertex(0) == p.vertex(0));
		Assert.assertTrue(s.base().vertex(3) == p.vertex(3));
		Assert.assertTrue(s.height() == 10);
	}

	@Test
	public void testEnclosesPoint()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Shape s = new Shape();
		s.setBase(p);
		s.setHeight(10);

		Assert.assertTrue(s.encloses(new Point(0, 0, 0)));
		Assert.assertTrue(s.encloses(new Point(5, 5, 5)));
		Assert.assertTrue(!s.encloses(new Point(15, 15, 15)));
	}

	@Test
	public void testEnclosesShape()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Polygon p2 = new Polygon(p);
		p2.subtract(1);
		Shape insider = new Shape();
		insider.setBase(p2);
		insider.setHeight(10);

		Shape s = new Shape();
		s.setBase(p);
		s.setHeight(10);

		Assert.assertTrue(s.encloses(insider));
		Assert.assertTrue(s.encloses(s));

		p2.subtract(-5);
		insider.setBase(p2);
		Assert.assertTrue(!s.encloses(insider));
	}
}
