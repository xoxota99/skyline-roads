package com.skyline.roadsys.geometry;

import java.util.*;

import javax.vecmath.*;

import org.junit.*;

import com.skyline.roadsys.util.*;

public class TestPolygon {

	@Test
	public void testInterface()
	{
		Polygon p = new Polygon();

		p.addVertex(new Point(1, 1));
		p.addVertex(new Point(-1, 1));
		p.addVertex(new Point(-1, -1));
		p.addVertex(new Point(1, -1));

		Assert.assertEquals(new Point(1, 1), p.vertex(0));
		Assert.assertEquals(new Point(-1, 1), p.vertex(1));
		Assert.assertEquals(new Point(-1, -1), p.vertex(2));
		Assert.assertEquals(new Point(1, -1), p.vertex(3));

		Assert.assertEquals(4, p.area(), Units.EPSILON);
		Assert.assertEquals(new Point(0, 0), p.centroid());

		Assert.assertTrue(p.encloses2D(new Point(0, 0)));
		Assert.assertTrue(p.encloses2D(new Point(-1, 0)));
		Assert.assertTrue(p.encloses2D(new Point(-0.5, 0)));
		Assert.assertTrue(p.encloses2D(new Point(-0.5, 1)));
		Assert.assertTrue(p.encloses2D(new Point(-0.5, 0.5)));

		p.removeVertex(2);
		Assert.assertTrue(p.vertex(2).equals(new Point(1, -1)));

		Assert.assertEquals(2, p.area(), Units.EPSILON);
	}

	@Test
	public void testcentroid()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Assert.assertEquals(new Point(5, 5), p.centroid());

		p = new Polygon(new Point(-10, -10),
				new Point(-10, 10),
				new Point(10, 10),
				new Point(10, -10));

		Assert.assertEquals(new Point(0, 0), p.centroid());
	}

	@Test
	public void testEdgeAccess()
	{
		Polygon p = new Polygon(new Point(0, 0), new Point(1, 1));
		Assert.assertTrue(new LineSegment(new Point(0, 0), new Point(1, 1)).equals(p.edge(0)));

		p.addVertex(new Point(1, 0));
		Assert.assertTrue(new LineSegment(new Point(0, 0), new Point(1, 1)).equals(p.edge(0)));
	}

	@Test
	public void testNormal()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(1, 1),
				new Point(1, 0),
				new Point(10, 10),
				new Point(10, 20));

		Assert.assertEquals(new Vector3d(0, 0, 1), p.normal());
	}

	@Test
	public void testEdgeNormal()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 10),
				new Point(10, 0));
		Assert.assertEquals(new Vector3d(-1, 0, 0), p.edgeNormal(1));
	}

	@Test
	public void testEdgeNormal2()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(0, 10),
				new Point(10, 10));

		Assert.assertEquals(new Vector3d(0, -1, 0), p.edgeNormal(1));
	}

	@Test
	public void testEdgeNormal3()
	{
		Polygon p = new Polygon(new Point(-10, -10),
				new Point(-10, 10),
				new Point(10, 10),
				new Point(10, -10));

		Assert.assertEquals(new Vector3d(1, 0, 0), p.edgeNormal(0));
		Assert.assertEquals(new Vector3d(0, -1, 0), p.edgeNormal(1));
	}

	@Test
	public void testEdgeNormal4()
	{
		Polygon p = new Polygon(new Point(-100, -100, 0), new Point(100, -100, 0), new Point(100, 100, 0));

		Assert.assertEquals(new Vector3d(0, 1, 0), p.edgeNormal(0));
	}

	@Test
	public void testZRotation()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		p.rotate(0, 0, 90);
		Assert.assertEquals(new Point(10, 0), p.vertex(0));
		Assert.assertEquals(new Point(10, 10), p.vertex(1));
		Assert.assertEquals(new Point(0, 10), p.vertex(2));
		Assert.assertEquals(new Point(0, 0), p.vertex(3));
	}

	@Test
	public void testScale()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		p.scale(2);

		Assert.assertEquals(new Point(-5, -5), p.vertex(0));
		Assert.assertEquals(new Point(15, -5), p.vertex(1));
		Assert.assertEquals(new Point(15, 15), p.vertex(2));
		Assert.assertEquals(new Point(-5, 15), p.vertex(3));
	}

	@Test
	public void testSubtract()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		p.subtract(1);
		Assert.assertEquals(new Point(1, 1), p.vertex(0));
		Assert.assertEquals(new Point(9, 1), p.vertex(1));
		Assert.assertEquals(new Point(9, 9), p.vertex(2));
		Assert.assertEquals(new Point(1, 9), p.vertex(3));
	}

	@Test
	public void testSubstractEdge()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		p.substractEdge(0, 2);
		Assert.assertEquals(new Point(0, 2), p.vertex(0));
		Assert.assertEquals(new Point(10, 2), p.vertex(1));
		Assert.assertEquals(new Point(10, 10), p.vertex(2));
		Assert.assertEquals(new Point(0, 10), p.vertex(3));
	}

	@Test
	public void testIsPolygonNonSelfIntersecting()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Assert.assertTrue(p.isNonSelfIntersecting());

		p.addVertex(new Point(5, -1));
		Assert.assertTrue(!p.isNonSelfIntersecting());
	}

	@Test
	public void testIsSimple()
	{
		Polygon p = new Polygon(new Point(-877.384, 788.152, 80),
				new Point(-963.825, 788.152, 80),
				new Point(-963.825, 793.44, 80),
				new Point(-877.384, 735.813, 80));

		Assert.assertTrue(!p.isNonSelfIntersecting());
	}

	@Test
	public void testIsSimple2()
	{
		Polygon p = new Polygon(new Point(-801.718, -858.983, 140),
				new Point(-809.158, -874.198, 140),
				new Point(-758.78, -874.198, 140),
				new Point(-761.062, -878.864, 140));

		Assert.assertTrue(!p.isNonSelfIntersecting());
	}

	@Test
	public void testIsSimple3()
	{
		Polygon p = new Polygon(new Point(-824.696, -860.434, 120),
				new Point(-826.42, -863.96, 120),
				new Point(-787.174, -863.96, 120),
				new Point(-793.023, -875.921, 120));

		Assert.assertTrue(!p.isNonSelfIntersecting());
	}

	@Test
	public void testTriangulation()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		List<Point> triangles = p.triangulate();

		Assert.assertTrue(triangles.size() == 6);
	}

	@Test
	public void testSplit()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Line line = new Line(new Point(5, 0), new Point(5, 6));
		List<Polygon> newOnes = p.split(line);

		Assert.assertEquals(2, newOnes.size());
	}

	@Test
	public void testHarderSplit()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Line line = new Line(new Point(0, 0), new Point(10, 10));
		List<Polygon> newOnes = p.split(line);

		Assert.assertEquals(2, newOnes.size());
	}

	@Test
	public void testSplitOnEdge()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Line line = new Line(new Point(0, 0), new Point(10, 0));
		List<Polygon> newOnes = p.split(line);

		Assert.assertEquals(1, newOnes.size());
	}

	@Test
	public void testSplitOnVertex()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Line line = new Line(new Point(-1, 1), new Point(1, -1));
		List<Polygon> newOnes = p.split(line);

		Assert.assertEquals(1, newOnes.size());
	}

	@Test
	public void testSplitOutOfPolygon()
	{
		Polygon p = new Polygon(new Point(0, 0),
				new Point(10, 0),
				new Point(10, 10),
				new Point(0, 10));

		Line line = new Line(new Point(-1, 0), new Point(-1, 10));
		List<Polygon> newOnes = p.split(line);

		Assert.assertEquals(1, newOnes.size());
	}

	@Test
	public void testWeirdSplitCase()
	{
		Polygon p = new Polygon(new Point(447.439, 1950.5, 0),
				new Point(568.503, 1950.5, 0),
				new Point(568.503, 2049.5, 0),
				new Point(355.508, 2049.5, 0));

		Line line = new Line(new Point(365.508, 2049.5, 0), new Point(365.508, 2048.5, 0));
		List<Polygon> newOnes = p.split(line);
		// DEBUG: original split:Line(Point(355.508, 2049.5, 0), Point(355.508,
		// 2048.5, 0))
		Assert.assertEquals(2, newOnes.size());
	}
}
