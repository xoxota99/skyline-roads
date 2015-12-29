package com.skyline.roadsys.area;

import java.util.*;

import org.apache.commons.logging.*;
import org.junit.*;

import com.skyline.roadsys.geometry.*;

public class TestBlock {

	private Log logger = LogFactory.getLog(this.getClass());

	@Test
	public void testSubdivisionAlgorithm() {
		Polygon p = new Polygon(new Point(0, 0),
				new Point(200, 0),
				new Point(200, 200),
				new Point(0, 200));

		Block b = new Block(null, p);

		b.createLots(50, 50, 0.1);

		List<Lot> lots = b.getLots();

		Assert.assertNotNull(lots);
		Assert.assertNotEquals(0, lots.size());

		logger.debug("" + lots.size() + " lots created.");
		for (Lot lot : lots) {
			logger.debug(lot);
		}
	}

}
