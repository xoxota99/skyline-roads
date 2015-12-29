package com.skyline;

import java.util.*;

import javax.vecmath.*;

import org.apache.commons.logging.*;

import com.skyline.building.*;
import com.skyline.roadsys.*;
import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.lsystem.roads.*;
import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.streetgraph.pattern.*;
import com.skyline.roadsys.util.*;

public class TestCity extends City {

	public static double meter = 5;
	private Log logger = LogFactory.getLog(this.getClass());
	private double allotmentWidth;
	private double allotmentDepth;

	public static void main(String... args){
		TestCity tc = new TestCity();
		tc.generate();
	}
	
	public TestCity() {
		init();
	}

	protected void createPrimaryRoadNetwork() {
		logger.debug("STAGE 1: Generating primary road network ...");

		OrganicRoadPattern generator = new OrganicRoadPattern();

		generator.setTarget(map);
		generator.setAreaConstraints(area);
		generator.setRoadType(RoadType.PRIMARY);
		// generator.setRoadLength(600, 800);
		// generator.setSnapDistance(200);
		generator.setRoadLength(180, 240);	// City is 1000x1000. Does this puts every road outside the city limits?
		generator.setSnapDistance(80);

		generator.setInitialPosition(area.centroid());
		generator.generate();

		/*
		 * This is important to avoid filament roads to go under buildings and
		 * such.
		 */
		//map.removeFilamentRoads();

		logger.info("===============");
		logger.info(map.toString());
	}

	protected void createZones() {

		logger.debug("STAGE 2: Creating city zones ...");

		zones = map.findZones();
	}

	protected void createSecondaryRoadNetwork() {
		logger.debug("STAGE 3: Generating secondary road network ...");

		for (Zone zone : zones)
		{
			RoadLSystem generator = new RasterRoadPattern();
			generator.setTarget(map);

			generator.setAreaConstraints(new Polygon(zone.areaConstraints()));
			generator.setRoadType(RoadType.SECONDARY);
			generator.setInitialPosition(zone.areaConstraints().centroid());
			generator.setInitialDirection(new Vector3d(0.2, 0.3, 0));
			generator.setRoadLength(60, 66);
			generator.setSnapDistance(30);
			generator.generate();

		}
		map.removeFilamentRoads();

		logger.debug("\t\tTotal of " + map.getRoads().size() + " road segments.");
	}

	protected void createBlocks() {

		logger.debug("STAGE 4: Creating blocks and allotments ...");

		int numberOfAllotments = 0, numberOfBlocks = 0;
		for (Zone zone : zones)
		{
			zone.createBlocks();
			List<Block> blocks = zone.getBlocks();
			numberOfBlocks += blocks.size();
			for (Block blocksIterator : blocks)
			{
				blocksIterator.createLots(allotmentWidth, allotmentDepth, 0);
				numberOfAllotments += blocksIterator.getLots().size();
			}
		}

		logger.debug("\t\t" + zones.size() + " zones.");
		logger.debug("\t\t" + numberOfBlocks + " blocks.");
		logger.debug("\t\t" + numberOfAllotments + " allotments.");
	}

	protected void createBuildings() {
		logger.debug("STAGE 5: Generating buildings ...");

		Point cityCenter = area.centroid();
		double distanceToCenter;

		int i = 0;
		for (Zone zone : zones)
		{
			List<Block> blocks = zone.getBlocks();
			for (Block block : blocks)
			{
				List<Lot> lots = block.getLots();
				for (Lot lot : lots)
				{
					logger.debug("BuildingLSystem " + i);

					Building building;

					/* Discard small lots. */
					if (lot.areaConstraints().area() < 5000)
						continue;

					distanceToCenter = lot.areaConstraints().vertex(0).distance(cityCenter);

					/*
					 * City center, that means sky scrapers, older tall
					 * buildings, occasionally some small historic building.
					 */

					/*
					 * TODO: Replace this with buildingMaxHeight from a
					 * rasterMap.
					 */
					if (distanceToCenter < 1500)
					{
						switch (MathUtil.randomInt(0, 2))
						{
							case 0:
								building = new Skyscraper(lot);

								if (distanceToCenter < 500)
								{
									if (MathUtil.random.nextDouble() <= 0.7)
									{
										building.setMaxHeight((MathUtil.randomInt(20, 30)) * 2.5 * Units.METER);
									}
									else
									{
										building.setMaxHeight((MathUtil.randomInt(10, 15)) * 2.5 * Units.METER);
									}
								}
								else if (distanceToCenter < 1000)
								{
									building.setMaxHeight((MathUtil.randomInt(5, 20)) * 2.5 * Units.METER);
								}
								break;
							case 1:
								building = new RedBuilding(lot);

								if (distanceToCenter < 500)
								{
									if (MathUtil.random.nextDouble() <= 0.7)
									{
										building.setMaxHeight((MathUtil.randomInt(15, 25)) * 2.5 * Units.METER);
									}
									else
									{
										building.setMaxHeight((MathUtil.randomInt(7, 10)) * 2.5 * Units.METER);
									}
								}
								else if (distanceToCenter < 1000)
								{
									building.setMaxHeight((MathUtil.randomInt(3, 15)) * 2.5 * Units.METER);
								}
								break;
							case 2:
								building = new OldBuilding(lot);

								if (distanceToCenter < 500)
								{
									if (MathUtil.random.nextDouble() <= 0.7)
									{
										building.setMaxHeight((MathUtil.randomInt(10, 15)) * 2.5 * Units.METER);
									}
									else
									{
										building.setMaxHeight((MathUtil.randomInt(7, 10)) * 2.5 * Units.METER);
									}
								}
								else if (distanceToCenter < 1000)
								{
									building.setMaxHeight((MathUtil.randomInt(3, 15)) * 2.5 * Units.METER);
								}
								break;
							default:
								throw new IllegalStateException("Wrong building configuration");
						}
					}
					else
					/*
					 * Peripheral and suburban areas. Small residential houses.
					 * Family houses.
					 */
					{
						switch (MathUtil.randomInt(0, 3))
						{
							case 0:
								building = new RedBuilding(lot);
								break;
							case 1:
							case 2:
								building = new SuburbanHouse(lot);
								break;
							case 3:
								building = new OldBuilding(lot);
								break;
							default:
								throw new IllegalStateException("Wrong building configuration");
						}

						building.setMaxHeight((MathUtil.randomInt(3, 7)) * 2.5 * Units.METER);
					}

					building.render();
				}
			}
		}

		logger.debug("Total of " + i + " buildings rendered.");
	}

	protected void renderRoadNetwork() {

		// StreetGraphRenderer* streetGraphRenderer = new
		// StreetGraphRenderer(sceneManager);
		// streetGraphRenderer.setStreetGraph(map);
		// streetGraphRenderer.setTerrain(terrain);
		//
		// streetGraphRenderer.setRoadSampleLength(1);
		// streetGraphRenderer.setNumberOfVerticesPerSample(5);
		//
		// streetGraphRenderer.setRoadParameters(Road::PRIMARY_ROAD,
		// primaryRoad);
		// streetGraphRenderer.setRoadParameters(Road::SECONDARY_ROAD,
		// secondaryRoad);
		// streetGraphRenderer.render();

	}

	protected void renderBuildings() {
		Point cityCenter = area.centroid();
		double distanceToCenter;

		// Ogre::SceneNode* zoneNode, * blockNode;

		int i = 0;
		for (Zone zone : zones)
		{
			// zoneNode =
			// sceneManager.getRootSceneNode().createChildSceneNode();

			List<Block> blocks = zone.getBlocks();
			for (Block block : blocks)
			{
				// blockNode = zoneNode.createChildSceneNode();

				List<Lot> lots = block.getLots();
				for (Lot lot : lots)
				{
					logger.debug("Building " + (i));

					Building building;

					/* Discard small lots. */
					if (lot.areaConstraints().area() < 5000)
						continue;

					distanceToCenter = cityCenter.distance(lot.areaConstraints().vertex(0));

					/*
					 * City center, that means sky scrapers, older tall
					 * buildings, occasionaly some small historic building.
					 */
					// TODO: Get this from a buildingHeight RasterMap
					if (distanceToCenter < 1500)
					{
						switch (MathUtil.randomInt(0, 2))
						{
							case 0:
								building = new Skyscraper(lot);

								if (distanceToCenter < 500)
								{
									if (MathUtil.random.nextDouble() <= 0.7)
									{
										building.setMaxHeight(MathUtil.randomInt(20, 30) * 2.5 * Units.METER);
									}
									else
									{
										building.setMaxHeight(MathUtil.randomInt(10, 15) * 2.5 * Units.METER);
									}
								}
								else if (distanceToCenter < 1000)
								{
									building.setMaxHeight(MathUtil.randomInt(5, 20) * 2.5 * Units.METER);
								}
								break;
							case 1:
								building = new RedBuilding(lot);

								if (distanceToCenter < 500)
								{
									if (MathUtil.random.nextDouble() <= 0.7)
									{
										building.setMaxHeight(MathUtil.randomInt(15, 25) * 2.5 * Units.METER);
									}
									else
									{
										building.setMaxHeight(MathUtil.randomInt(7, 10) * 2.5 * Units.METER);
									}
								}
								else if (distanceToCenter < 1000)
								{
									building.setMaxHeight(MathUtil.randomInt(3, 15) * 2.5 * Units.METER);
								}
								break;
							case 2:
								building = new OldBuilding(lot);

								if (distanceToCenter < 500)
								{
									if (MathUtil.random.nextDouble() <= 0.7)
									{
										building.setMaxHeight(MathUtil.randomInt(10, 15) * 2.5 * Units.METER);
									}
									else
									{
										building.setMaxHeight(MathUtil.randomInt(7, 10) * 2.5 * Units.METER);
									}
								}
								else if (distanceToCenter < 1000)
								{
									building.setMaxHeight(MathUtil.randomInt(3, 15) * 2.5 * Units.METER);
								}
								break;
							default:
								throw new IllegalStateException("Wrong building configuration");
						}
					}
					else
					/*
					 * Peripheral and suburban areas. Small residential houses.
					 * Familly houses.
					 */
					{
						switch (MathUtil.randomInt(0, 3))
						{
							case 0:
								building = new RedBuilding(lot);
								break;
							case 1:
							case 2:
								building = new SuburbanHouse(lot);
								break;
							case 3:
								building = new OldBuilding(lot);
								break;
							default:
								throw new IllegalStateException("Wrong building configuration");
						}

						building.setMaxHeight(MathUtil.randomInt(3, 7) * 2.5 * Units.METER);
					}

					building.render();
				}
			}
		}

		logger.debug("Total of " + (i) + " buildings rendered.");
	}

	/**
	 * Inline method that is called in the constructor. This is the only place
	 * for any parameter initialization and configuration.
	 */
	protected void init()
	{
		MathUtil.initRandom(1); // std::time(0)

		/* Define city boundaries >> */
		//City is 500x500.
		area.addVertex(new Point(500, 500));
		area.addVertex(new Point(500, -500));
		area.addVertex(new Point(-500, -500));
		area.addVertex(new Point(-500, 500));

		map.addRoad(new Path(500, 500, 500, -500));
		map.addRoad(new Path(500, -500, -500, -500));
		map.addRoad(new Path(-500, -500, -500, 500));
		map.addRoad(new Path(-500, 500, 500, 500));

		RoadType.PRIMARY.setWidth(3.3);
		RoadType.SECONDARY.setWidth(2);

		/* How big will be the lots for houses */
		allotmentWidth = 150;
		allotmentDepth = 150;
	}

}
