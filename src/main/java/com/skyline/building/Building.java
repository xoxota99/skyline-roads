package com.skyline.building;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.*;
import com.skyline.roadsys.lsystem.buildings.*;

//From OgreBuilding
public abstract class Building extends BuildingLSystem {

	private String UID = this.getClass().getName() + "_" + System.currentTimeMillis();

	protected double storeyHeight;
	protected double basementHeight;
	protected double spacerHeight;
	protected double ledgeHeight;
	protected double tileWidth;
	protected double rooftopHeight;

	public Building(Lot parentLot) {
		super(parentLot);
		initialize();
	}

	protected void configure() {
		setAxiom("{BFER}");

		setInitialDirection(new Vector3d(0, 0, 1));	//already done in initialize?

		storeyHeight = 20;
		basementHeight = 20;
		spacerHeight = 5;
		tileWidth = 40;
	}

	protected void interpretSymbol(char symbol) {

		switch (symbol)
		{
			case 'B':
				drawBasement();
				break;
			case 'F':
				drawStorey();
				break;
			case 'G':
				drawFloor();
				break;
			case 'S':
				drawSpacer();
				break;
			case 'O':
				drawObliqueLedge();
				break;
			case 'L':
				drawLedge();
				break;
			case 'R':
				drawRooftop();
				break;
			case 'H':
				drawHouseRooftop();
				break;
			case '-':
				substractBoundingBox(5);
				break;
			case '+':
				addBoundingBox(5);
				break;
			default:
				/* Try to interpret symbols defined in parent. */
				super.interpretSymbol(symbol);
				break;
		}
	}

	protected String getUniqueObjectName() {
		return this.UID;
	}

	protected void stopGrowth() {
		currentlyInterpretedSymbol = 0;
		while (currentlyInterpretedSymbol < producedString.size())
		{
			Symbol symbol = producedString.get(0);
			char symbolChar = symbol.getSymbol();
			currentlyInterpretedSymbol++;

			producedString.remove(0);

			if (symbolChar == 'E')
			{
				break;
			}
		}
	}

	protected void drawBasement() {
		// Point lowerBound = cursor.getPosition();
		cursor.move(basementHeight);
		// Point higherBound = cursor.getPosition();
	}

	protected void drawFloor() {
		// renderFloor(floorMaterial);
	}

	protected void drawStorey() {
		// Point lowerBound = cursor.getPosition();
		cursor.move(storeyHeight);
		// Point higherBound = cursor.getPosition();
	}

	protected void drawSpacer() {
		// Point lowerBound = cursor.getPosition();
		cursor.move(spacerHeight);
		// Point higherBound = cursor.getPosition();

		// renderStorey(spacerMaterial, lowerBound, higherBound);
	}

	protected void drawLedge() {
		addBoundingBox(1);
		drawFloor();

		// Point lowerBound = cursor.getPosition();
		cursor.move(ledgeHeight);
		// Point higherBound = cursor.getPosition();

		// renderStorey(ledgeMaterial, lowerBound, higherBound);

		drawFloor();
		substractBoundingBox(1);
	}

	protected void renderStorey(Point bottom, Point top)
	{
		Polygon base = boundingBox.base();
		Polygon wall = new Polygon();
		Vector3d normal;
		Vector3d floorOffset = new Vector3d(0, 0, bottom.z);
		Vector3d ceilingOffset = new Vector3d(0, 0, top.z);
		int current, next;
		for (int i = 0; i < base.numberOfVertices(); i++)
		{
			current = i;
			next = (i + 1) % base.numberOfVertices();

			normal = base.edgeNormal(current);
			normal.negate();

			wall.clear();
			wall.addVertex(base.vertex(current).plus(floorOffset));
			wall.addVertex(base.vertex(next).plus(floorOffset));
			wall.addVertex(base.vertex(next).plus(ceilingOffset));
			wall.addVertex(base.vertex(current).plus(ceilingOffset));

			addPlane(wall, normal);
		}
	}

	protected void addPlane(Polygon plane, Vector3d surfaceNormal)
	{
		assert (plane.numberOfVertices() == 4);

		/**
		 * <pre>
		 * Plane must be defined in the following manner (CCW):
		 * 	     |<---------^
		 * 	     |          |
		 * 	     v[0]-----.|
		 * 	    Otherwise expect unexpected results.
		 * </pre>
		 */
		int horizontalTiles = (int) (plane.edge(0).length() / tileWidth);
		horizontalTiles = horizontalTiles == 0 ? 1 : horizontalTiles;

		double tileBottomWidth = plane.edge(0).length() / horizontalTiles, tileTopWidth = plane.edge(2).length() / horizontalTiles; // plane.edge(0).length()
																																	// /
																																	// horizontalTiles;

		Vector3d planeNormal = new Vector3d(plane.normal());
		Vector3d bottomEdgeDirection = new Vector3d(plane.vertex(1));
		bottomEdgeDirection.sub(plane.vertex(0));
		bottomEdgeDirection.normalize();

		Vector3d topEdgeDirection = new Vector3d(plane.vertex(2));
		topEdgeDirection.sub(plane.vertex(3));
		topEdgeDirection.normalize();

		Polygon tile = new Polygon();

		for (int i = 0; i < (horizontalTiles); i++)
		{
			// buildingObject.begin(material);
			tile.clear();
			Point bot = new Point(bottomEdgeDirection);
			bot.scale(tileBottomWidth);
			Point top = new Point(topEdgeDirection);
			bot.scale(tileTopWidth);

			Point v0 = new Point(bot);
			v0.scale(i);
			v0.add(plane.vertex(0));
			tile.addVertex(v0);

			Point v1 = new Point(bot);
			v1.scale(i + 1);
			v1.add(plane.vertex(0));
			tile.addVertex(v1);

			Point v2 = new Point(top);
			v2.scale(i + 1);
			v2.add(plane.vertex(3));
			tile.addVertex(v2);

			Point v3 = new Point(top);
			v3.scale(i);
			v3.add(plane.vertex(3));
			tile.addVertex(v3);

			// buildingObject.position(OgreCity::libcityToOgre(tile.vertex(0)));
			// buildingObject.textureCoord(1,1);
			// buildingObject.position(OgreCity::libcityToOgre(tile.vertex(1)));
			// buildingObject.textureCoord(0,1);
			// buildingObject.position(OgreCity::libcityToOgre(tile.vertex(2)));
			// buildingObject.textureCoord(0,0);
			// buildingObject.position(OgreCity::libcityToOgre(tile.vertex(3)));
			// buildingObject.textureCoord(1,0);

			if (planeNormal == surfaceNormal)
			{
				// buildingObject.quad(0, 1, 2, 3);
			}
			else
			{
				// buildingObject.quad(0, 3, 2, 1);
			}

			// buildingObject.end();
		}

		/* Last tile */

	}

	protected void drawObliqueLedge() {
		// Polygon bottomBase = boundingBox.base();

		// Point lowerBound = cursor.getPosition();
		cursor.move(ledgeHeight);
		// Point higherBound = cursor.getPosition();

		addBoundingBox(2);
		// Polygon topBase = boundingBox.base();

		// renderObliqueShape(ledgeMaterial, bottomBase, topBase, lowerBound,
		// higherBound);

		drawFloor();
		substractBoundingBox(2);
	}

	protected void drawRooftop() {
		renderFloor();
	}

	protected void renderFloor() {

		Polygon base = boundingBox.base();
		assert (base.numberOfVertices() > 0);
		Polygon roof = new Polygon();
		Point roofVertex;
		Point min, max;
		double currentHeight = cursor.getPosition().z;

		min = base.vertex(0);
		max = base.vertex(0);
		for (int i = 0; i < base.numberOfVertices(); i++)
		{
			/* update minimum and maximum values */
			if (min.x > base.vertex(i).x)
				min.x = (base.vertex(i).x);
			if (min.y > base.vertex(i).y)
				min.y = (base.vertex(i).y);
			if (max.x < base.vertex(i).x)
				max.x = (base.vertex(i).x);
			if (max.y < base.vertex(i).y)
				max.y = (base.vertex(i).y);

			roofVertex = base.vertex(i);
			roofVertex.z = (currentHeight);
			roof.addVertex(roofVertex);
		}

		// double polygonWidth = max.x - min.x;
		// double polygonHeight = max.y - min.y;

		// buildingObject.begin(material);
		//
		// for (int i = 0; i < roof.numberOfVertices(); i++)
		// {
		// buildingObject.position(OgreCity::libcityToOgre(roof.vertex(i)));
		// buildingObject.textureCoord((roof.vertex(i).x - min.x) /
		// polygonWidth, (roof.vertex(i).y - min.y) / polygonWidth);
		// }

		Vector3d planeNormal = roof.normal();
		List<Integer> triangles = roof.getSurfaceIndexes();
		assert (triangles.size() % 3 == 0);
		int numberOfTriangles = triangles.size() / 3;

		Vector3d Z_UNIT = new Vector3d(0, 0, 1);
		for (int i = 0; i < numberOfTriangles; i++)
		{
			// buildingObject.triangle(triangles[3*i+2], triangles[3*i+1],
			// triangles[3*i+0]);
			// buildingObject.triangle(triangles[3*i+0], triangles[3*i+1],
			// triangles[3*i+2]);
			if (planeNormal.equals(Z_UNIT))
			{
			}
			else
			{
			}
		}

		// buildingObject.end();
	}

	public void render() {
		while (readNextSymbol() != 0)
		{
			if (!boundingBox.encloses(cursor.getPosition()))
			{
				finishDrawing();
				break;
			}
		}

		// String name = buildingObject.getName();
		// buildingObject.convertToMesh(name + "Mesh");

		// Ogre::SceneNode* sceneNode =
		// parentSceneNode.createChildSceneNode(name+"Node");
		// Ogre::StaticGeometry* staticObject =
		// sceneManager.createStaticGeometry(name+"Static");

		// Ogre::Entity* entity;
		// entity = sceneManager.createEntity(name + "Entity", name + "Mesh");
		// entity.setCastShadows(true);
		// staticObject.addEntity(entity, Ogre::Vector3(0,0,0));
		// staticObject.setCastShadows(true);
		// staticObject.build();
		// sceneNode.attachObject(entity);
	}

	protected void drawHouseRooftop() {

		Polygon bottomBase = new Polygon(boundingBox.base());

		Point lowerBound = new Point(cursor.getPosition());
		cursor.move(rooftopHeight);
		Point higherBound = new Point(cursor.getPosition());

		/* Subtract bounding box to minimum */
		Polygon base = new Polygon(boundingBox.base());
		Polygon baseCopy = new Polygon(base);
		double constraintsArea = parentLot.areaConstraints().area();
		while (base.isNonSelfIntersecting() && boundingBox.encloses(base) &&
				base.area() > 0.1 * constraintsArea)
		{
			baseCopy = new Polygon(base);
			base.subtract(1);
		}
		boundingBox.setBase(baseCopy);

		Polygon topBase = new Polygon(boundingBox.base());

		renderObliqueShape(bottomBase, topBase, lowerBound, higherBound);

		drawRooftop();
	}

	private void renderObliqueShape(Polygon bottomBase, Polygon topBase,
			Point beginningHeight, Point endHeight)
	{
		assert (bottomBase.numberOfVertices() == topBase.numberOfVertices());
		assert (bottomBase.isClosed());

		Vector3d floorOffset = new Vector3d(0, 0, beginingHeight.z);
		Vector3d ceilingOffset = new Vector3d(0, 0, endHeight.z);

		Vector3d normal;
		int current, next;
		for (int number = 0; number < bottomBase.numberOfVertices(); number++)
		{
			current = number;
			next = (number + 1) % bottomBase.numberOfVertices();

			normal = bottomBase.edgeNormal(current);
			normal.negate();

			Polygon plane = new Polygon();
			Point p = new Point(bottomBase.vertex(current));
			p.add(floorOffset);
			plane.addVertex(p);

			p = new Point(bottomBase.vertex(next));
			p.add(floorOffset);
			plane.addVertex(p);

			p = new Point(bottomBase.vertex(next));
			p.add(ceilingOffset);
			plane.addVertex(p);

			p = new Point(bottomBase.vertex(current));
			p.add(ceilingOffset);
			plane.addVertex(p);

			addPlane(plane, normal);
		}
	}

	protected void substractBoundingBox(double byThisMuch) {
		// debug("OgreBuilding::substractBoundingBox()");

		if (boundingBox.base().area() < 0.5 * parentLot.areaConstraints().area())
		{
			// smaller than half the size of the lot. Too small.
			return;
		}

		Polygon base = boundingBox.base();
		base.subtract(byThisMuch);
		if (base.isNonSelfIntersecting() && boundingBox.encloses(base))
		{
			boundingBox.setBase(base);
		}
	}

	protected void addBoundingBox(double byThisMuch) {
		// debug("OgreBuilding::addBoundingBox()");

		Polygon base = boundingBox.base();
		base.subtract(-byThisMuch);
		if (base.isNonSelfIntersecting())
		{
			boundingBox.setBase(base);
		}
	}

	protected void finishDrawing() {
		drawRoofTop();
	}

	private void drawRoofTop() {
		renderFloor();
	}

	/**
	 * <ul>
	 * <li>B: draw basement</li>
	 * <li>F: draw floor</li>
	 * <li>S: draw spacer between floors</li>
	 * <li>L: draw ledge between floors (with substracting bounding box)</li>
	 * <li>O: draw oblique ledge between floors (with substracting bounding box)
	 * </li>
	 * <li>R: draw flat rooftop</li>
	 * <li>H: draw howe rooftop</li>
	 * <li>E: floor expansion</li>
	 * <li>-: substract bounding box</li>
	 * <li>+: add to bounding box</li>
	 */
	protected void initialize() {
		addToAlphabet("BFSOLRHE-+");
		configure();
	}
}
