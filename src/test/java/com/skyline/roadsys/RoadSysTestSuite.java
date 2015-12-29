package com.skyline.roadsys;

import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.*;
import com.skyline.roadsys.lsystem.rendering.*;
import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.streetgraph.pattern.*;

@RunWith(Suite.class)
@SuiteClasses({
		TestBlock.class,
		TestLot.class,
		TestSubRegion.class,
		TestZone.class,

		TestLine.class,
		TestLineSegment.class,
		TestPoint.class,
		TestPolygon.class,
		TestRay.class,
		TestShape.class,

		TestGraphicLSystem.class,
		TestLSystem.class,
		TestRasterRoadPattern.class,
		TestAreaExtractor.class,
		TestPath.class,
		TestStreetGraph.class
})
public class RoadSysTestSuite {
}