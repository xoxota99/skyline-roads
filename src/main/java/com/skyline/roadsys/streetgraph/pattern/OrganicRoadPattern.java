package com.skyline.roadsys.streetgraph.pattern;

import javax.vecmath.*;

import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.roads.*;

public class OrganicRoadPattern extends RoadLSystem {
	public OrganicRoadPattern() {

		setAxiom("[[[-_E]+_E]_E]++_E");

		// Rules
		addRule('E', "[[-_E]+_E]_E");
		// addRule('E', "[[-_E]+_E]");
		// addRule('E', "_E");

		setInitialPosition(new Point(0, 0));
		setInitialDirection(new Vector3d(1, 0, 0));

		setTurnAngle(60, 90);
	}
}
