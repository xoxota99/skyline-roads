package com.skyline.roadsys.streetgraph;

import java.util.*;

import org.apache.commons.logging.*;
import org.junit.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;

public class TestAreaExtractor {
	private AreaExtractor mcb;
	private StreetGraph sg;
	private Log logger = LogFactory.getLog(this.getClass());

	@Before
	public void initSuite() {
		mcb = new AreaExtractor();
		sg = new StreetGraph();
	}

	@Test(timeout = 1000)
	public void testSingleCycle() {
		sg.addRoad(new Path(-100, 100, 100, 100));
		sg.addRoad(new Path(100, 100, 100, -100));
		sg.addRoad(new Path(100, -100, -100, -100));
		sg.addRoad(new Path(-100, -100, -100, 100));

		List<Zone> cycles = mcb.extractZones(sg, null);
		Assert.assertEquals(1, cycles.size());
	}

	@Test(timeout = 1000)
	public void testFilament() {
		sg.addRoad(new Path(-100, 100, 100, 100));

		List<Zone> cycles = mcb.extractZones(sg, null);
		Assert.assertEquals(0, cycles.size());
	}

	@Test(timeout = 1000)
	public void testCycleOutOfZone() {

		sg.addRoad(new Path(-100, 100, 100, 100));
		sg.addRoad(new Path(100, 100, 100, -100));
		sg.addRoad(new Path(100, -100, -100, -100));
		sg.addRoad(new Path(-100, -100, -100, 100));

		Polygon constraints = new Polygon(new Point(-1000, -900),
				new Point(-1000, -800),
				new Point(-500, -800));

		Zone zone = new Zone(sg);
		zone.setAreaConstraints(constraints);
		mcb = new AreaExtractor();

		List<Zone> cycles = mcb.extractZones(sg, zone);
		Assert.assertEquals(0, cycles.size());
	}

	@Test(timeout = 1000)
	public void testCycleInZone() {

		sg.addRoad(new Path(-100, 100, 100, 100));
		sg.addRoad(new Path(100, 100, 100, -100));
		sg.addRoad(new Path(100, -100, -100, -100));
		sg.addRoad(new Path(-100, -100, -100, 100));

		Polygon constraints = new Polygon(new Point(-1000, 0),
				new Point(1000, 1000),
				new Point(1000, -1000));

		Zone zone = new Zone(sg);
		zone.setAreaConstraints(constraints);
		mcb = new AreaExtractor();

		List<Zone> cycles = mcb.extractZones(sg, zone);
		Assert.assertEquals(1, cycles.size());
	}

	@Test(timeout = 1000)
	public void testExtractZones() {

		/* First 2 cycles */
		sg.addRoad(new Path(-100, 100, 100, 100));
		sg.addRoad(new Path(100, 100, 100, -100));
		sg.addRoad(new Path(100, -100, -100, -100));
		sg.addRoad(new Path(-100, -100, -100, 100));
		sg.addRoad(new Path(-100, -100, 100, 100));

		/* Second cycle */
		sg.addRoad(new Path(-100, 100, 0, 200));
		sg.addRoad(new Path(0, 200, 100, 100));

		/* Filament */
		sg.addRoad(new Path(100, -100, 100, -200));

		Polygon constraints = new Polygon(new Point(-100, 100),
				new Point(100, 100),
				new Point(100, -100),
				new Point(-100, -100));

		Zone zone = new Zone(sg);
		zone.setAreaConstraints(constraints);

		List<Zone> cycles = mcb.extractZones(sg, zone);

		Assert.assertEquals(2, cycles.size());
	}

	@Test	//(timeout = 1000)
	public void testExtractBlocks() {
		/* First 2 cycles */
		sg.addRoad(new Path(-100, 100, 100, 100));
		sg.addRoad(new Path(100, 100, 100, -100));
		sg.addRoad(new Path(100, -100, -100, -100));
		sg.addRoad(new Path(-100, -100, -100, 100));

		sg.addRoad(new Path(-100, -100, 100, 100));

		/* Second cycle */
		sg.addRoad(new Path(-100, 100, 0, 200));
		sg.addRoad(new Path(0, 200, 100, 100));

		/* Filament */
		sg.addRoad(new Path(100, -100, 100, -200));

		//SHOULD eliminate Second Cycle and Filament.
		Polygon constraints = new Polygon(new Point(-100, 100),
				new Point(100, 100),
				new Point(100, -100),
				new Point(-100, -100));

		Zone zone = new Zone(sg);
		zone.setAreaConstraints(constraints);

		RoadType.PRIMARY.setWidth(10d);

		logger.debug(zone);
		
		//SHOULD give two blocks.
		List<Block> blocks = mcb.extractBlocks(sg, zone);	
		
		logger.debug(blocks.get(0));
		
		Assert.assertEquals(2, blocks.size());
	}
}
