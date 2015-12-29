package com.skyline.roadsys.streetgraph.pattern;

import javax.vecmath.*;

import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.roads.*;

public class RasterRoadPattern extends RoadLSystem {

	public RasterRoadPattern() {
		setAxiom("E");

		// Rules
		addRule('E', "[[-_E]+_E]_E");
		// addRule('E', "[[-_E]+_E]");
		// addRule('E', "_E");

		setInitialPosition(new Point(0, 0));
		setInitialDirection(new Vector3d(1, 0, 0));

		setTurnAngle(90, 90);
	}

}
