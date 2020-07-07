package com.skyline.roadsys.streetgraph;

import javax.vecmath.*;

import org.junit.*;

import com.skyline.roadsys.geometry.*;

public class TestPath {

	@Test
	public void DirectionVectors()
	{
		Path path = new Path(new LineSegment(new Point(0, 0), new Point(10, 10)));
		Vector3d correct;

		correct = new Vector3d(-1, -1, 0);
		correct.normalize();
		Assert.assertEquals(correct, path.beginningDirectionVector());

		correct = new Vector3d(1, 1, 0);
		correct.normalize();
		Assert.assertEquals(correct, path.endDirectionVector());

		path.setBeginning(new Point(10, 10));
		path.setEnd(new Point(-10, -10));

		correct = new Vector3d(1, 1, 0);
		correct.normalize();

		Assert.assertEquals(correct, path.beginningDirectionVector());

		correct = new Vector3d(-1, -1, 0);
		correct.normalize();
		Assert.assertEquals(correct, path.endDirectionVector());
	}
}
