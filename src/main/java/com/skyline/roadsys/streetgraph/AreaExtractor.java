package com.skyline.roadsys.streetgraph;

import java.util.*;
import java.util.Map.Entry;

import javax.vecmath.*;

import org.apache.commons.logging.*;

import com.skyline.roadsys.area.*;
import com.skyline.roadsys.geometry.*;
import com.skyline.roadsys.util.*;

public class AreaExtractor {

	/** Stores all vertices of the graph sorted by their x position. */
	private List<Intersection> vertices;

	/** Graph description. List of adjacent nodes to each node. */
	private Map<Intersection, List<Intersection>> adjacentNodes;

	/** Edges marked as a part of a cycle. */
	private Set<Pair<Intersection, Intersection>> cycleEdges;

	// private Map<RoadType, Double> roadWidths;
	private boolean substractRoadWidthFromAreas;

	private StreetGraph map;

	private List<Polygon> cycles;

	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * Find all minimal cycles and return them as polygons.
	 */
	private void getMinimalCycles() {
		while (!vertices.isEmpty())
		{
			Intersection next;
			Intersection current = vertices.get(0); // get the first vertex in
													// the graph.

			List<Intersection> neighbors = getAdjacentNodes(current);

			if (neighbors.size() == 0)
			/* Isolated, no cycle possible. */
			{
				logger.debug("Extracting isolated vertex.");
				extractIsolatedVertex(current);
			}
			else if (neighbors.size() == 1)
			/* Remove filaments */
			{
				logger.debug("Extracting filament.");
				next = neighbors.get(0);
				extractFilament(current, next);
			}
			else
			/* Extract cycles. */
			{
				logger.debug("Extracting minimal cycle.");
				next = neighbors.get(0);
				extractMinimalCycle(current, next);
			}
		}
	}

	/**
	 * Modifies a Polygon, in place, to minimize cycles. Also modifies the list
	 * of distances in place.
	 * 
	 * @param minimalCycle
	 * @param distances
	 */
	private void minimalizeCycle(Polygon minimalCycle, List<Double> distances) { // BYREF
																					// everything.
		boolean isMinimal = false;
		while (!isMinimal)
		{
			int previous, current, next, verticesCount = minimalCycle.numberOfVertices();
			isMinimal = true;
			for (int i = 0; i < verticesCount; i++)
			{
				previous = (i - 1) < 0 ? verticesCount - 1 : i - 1;
				current = i;
				next = (i + 1) % verticesCount;

				if (previous == next)
				{
					break;
				}

				// Vector from current to previous.
				Vector3d first = new Vector3d(minimalCycle.vertex(previous));
				first.sub(minimalCycle.vertex(current));

				// Vector from current to next.
				Vector3d second = new Vector3d(minimalCycle.vertex(next));
				first.sub(minimalCycle.vertex(current));

				if (Math.abs(first.angle(second)) <= Units.EPSILON || Math.abs(first.angle(second) - Units.PI) <= Units.EPSILON)
				{
					minimalCycle.removeVertex(current);
					distances.remove(current);
					isMinimal = false;
					break;
				}
			}
		}
	}

	// TODO: Can't help thinking this could be a lot simpler...
	private List<Double> getSubtractDistance(List<Intersection> intersections) {
		/* Get width of all edges */
		List<Double> edgeWidth = new ArrayList<Double>();
		int current, next;
		Road road;
		for (int i = 0; i < intersections.size(); i++)
		{
			current = i;
			next = (i + 1) % intersections.size();

			road = map.getRoadBetweenIntersections(intersections.get(current), intersections.get(next));

			edgeWidth.add(road.getType().getWidth());
		}

		return edgeWidth;
	}

	private void subtractRoadWidths(Polygon minimalCycle, List<Double> distances) {

		assert (minimalCycle.numberOfVertices() == distances.size());

		Polygon oldArea = new Polygon(minimalCycle); // deep copy.

		int vertices = minimalCycle.numberOfVertices();
		Line previousEdge = new Line(), currentEdge = new Line();
		Point newVertex = new Point();

		for (int i = 0; i < vertices; i++)
		{
			int previous = (i - 1) < 0 ? vertices - 1 : i - 1;
			int current = i;
			int next = (i + 1) % vertices;

			// Counter-clockwise. Normals are coming back negated.
			Vector3d previousNormal = oldArea.edgeNormal(previous);
			Vector3d currentNormal = oldArea.edgeNormal(current);
			previousNormal.normalize();
			currentNormal.normalize();

			Point b = new Point(previousNormal);
			b.scale(distances.get(previous));
			b.add(oldArea.vertex(current));
			previousEdge.setBegining(b);

			Point e = new Point(previousNormal);
			e.scale(distances.get(previous));
			e.add(oldArea.vertex(previous));
			previousEdge.setEnd(e);

			b = new Point(currentNormal);
			b.scale(distances.get(current));
			b.add(oldArea.vertex(current));
			currentEdge.setBegining(b);

			e = new Point(currentNormal);
			e.scale(distances.get(current));
			e.add(oldArea.vertex(next));
			currentEdge.setEnd(e);

			IntersectionType type;
			type = currentEdge.intersection2D(previousEdge, newVertex);
			if (type == IntersectionType.PARALLEL)
			{
				double distance = distances.get(previous);
				if (distance < distances.get(current))
				{
					distance = distances.get(current);
				}
				Point n = new Point(currentNormal);
				n.scale(distance);
				n.add(oldArea.vertex(current));
				newVertex = n;
			}

			minimalCycle.updateVertex(current, newVertex);
		}
	}

	private void copyVertices(StreetGraph map, Zone zone) {
		reset();

		/* Add all nodes into adjacency list. */
		List<Intersection> intersections = map.getIntersections();
		for (Intersection intersection : intersections) {
			addVertex(intersection, zone);
		}
		for(Map.Entry<Intersection,List<Intersection>> e : adjacentNodes.entrySet()){
			logger.info("For Intersection: "+e.getKey().toString());
			for(Intersection i : e.getValue()){
				logger.info("\t"+i.toString());
			}
		}
	}

	/**
	 * Add all vertices that fall within the provided inclusion zone.
	 * 
	 * @param node
	 * @param zone
	 */
	private void addVertex(Intersection node, Zone zone) {

		List<Intersection> adjacent = node.adjacentIntersections();
		if (zone != null)
		{
			if (!zone.isIntersectionInside(node))
			{
				return;
			}

			// Filter adjacentnodes down to those that are in the zone.
			List<Intersection> adjacentNodesInZone = new ArrayList<Intersection>();
			for (Intersection i : adjacent)
			{
				if (zone.isIntersectionInside(i))
				{
					adjacentNodesInZone.add(i);
				}
			}
			adjacent.clear();

			adjacent.addAll(adjacentNodesInZone);

		}

		/* Insert adjacent intersections. */
		adjacentNodes.put(node, adjacent);

		int i = 0;
		Intersection existing;

		/* Determine where to sort current node. */
		Point p = node.position();

		//TODO: Should this be by distance from Origin?
		vertexLoop: for (i = 0; i < vertices.size(); i++)
		{
			existing = vertices.get(i);

			switch (p.compareTo(existing.position())) {
				case -1:// smaller
					break vertexLoop;
				case 0:// equal
					i++;
					break vertexLoop;
				case 1:// larger
					continue;
				default:
					throw new IllegalStateException("Point.compareTo returned some totally whacked out value! (was expecting -1,0, or 1, got " + node.position().compareTo(existing.position()));
			}
		}

		/* Insert into list. */
		vertices.add(i, node);
	}

	private void removeVertex(Intersection node) {
		vertices.remove(node);
		List<Intersection> l = adjacentNodes.get(node);
		for (Intersection adjacentNode : l)
		{
			removeEdge(node, adjacentNode);
		}

		adjacentNodes.remove(node);
	}

	/* Adding edges not necessary */
	private void removeEdge(Intersection begining, Intersection end) { // BYREF
																		// end
		/* Remove second point from adjacency list of first point. */
		List<Intersection> neighbors = adjacentNodes.get(begining);

		for (Intersection neighbor : neighbors)
		{
			if (neighbor.equals(end))
			{
				neighbors.remove(neighbor);
				break;
			}
		}

		/* And vice versa. */
		neighbors = adjacentNodes.get(end);

		for (Intersection neighbor : neighbors)
		{
			if (neighbor.equals(begining))
			{
				neighbors.remove(neighbor);
				break;
			}
		}

		/* If edge was marked as cycle edge, remove the mark as well. */
		cycleEdges.remove(new Pair<Intersection, Intersection>(begining, end));
		cycleEdges.remove(new Pair<Intersection, Intersection>(end, begining));
	}

	private boolean isCycleEdge(Intersection begining, Intersection end) {
		return cycleEdges.contains(new Pair<Intersection, Intersection>(begining, end)) ||
				cycleEdges.contains(new Pair<Intersection, Intersection>(end, begining));
	}

	private void markCycleEdge(Intersection begining, Intersection end) {
		cycleEdges.add(new Pair<Intersection, Intersection>(begining, end));
	}

	/* Extracting methods. */
	private void extractIsolatedVertex(Intersection vertex) {
		removeVertex(vertex);
	}

	// remove dead ends
	private void extractFilament(Intersection v0, Intersection v1) {

		// Is this edge marked as part of a cycle?
		if (isCycleEdge(v0, v1))
		{
			// Lots of neighbors? (>=3)
			if (numberOfAdjacentNodes(v0) >= 3)
			{
				// remove this edge
				removeEdge(v0, v1);

				// look at the end.
				v0 = v1;
				if (numberOfAdjacentNodes(v0) == 1) // whut.
				{
					v1 = firstAdjacentNode(v0);
				}
			}

			while (numberOfAdjacentNodes(v0) == 1)
			{
				// Cycle through this road, removing endpoints until we come to
				// an intersection with more than one neighbor.
				v1 = firstAdjacentNode(v0);
				if (isCycleEdge(v0, v1))
				{
					removeEdge(v0, v1);
					removeVertex(v0);
					v0 = v1;
				}
				else
				{
					break;
				}
			}
			
			//orphan vertex with no neighbors.
			if (numberOfAdjacentNodes(v0) == 0)
			{
				removeVertex(v0);
			}
		}
		else
		{
			if (numberOfAdjacentNodes(v0) >= 3)
			{
				removeEdge(v0, v1);
				v0 = v1;
				if (numberOfAdjacentNodes(v0) == 1)
				{
					v1 = firstAdjacentNode(v0);
				}
			}

			while (numberOfAdjacentNodes(v0) == 1)
			{
				v1 = firstAdjacentNode(v0);
				removeEdge(v0, v1);
				removeVertex(v0);
				v0 = v1;
			}

			if (numberOfAdjacentNodes(v0) == 0)
			{
				removeEdge(v0, v1);
				removeVertex(v0);
			}
		}
	}

	private void extractMinimalCycle(Intersection current, Intersection next) { // BYREF
																				// next

		SortedSet<Intersection> visited = new TreeSet<Intersection>();
		List<Intersection> sequence = new ArrayList<Intersection>();

		sequence.add(current);
		// next = getClockwiseMost(null, current); //WTF??? BYREF?
		next.set(getClockwiseMost(null, current));

		Intersection previousVertex = current;
		Intersection currentVertex = next;
		Intersection nextVertex = null;

		while ((currentVertex != null) &&
				(currentVertex != current) &&
				(!visited.contains(currentVertex)))
		{
			logger.debug("AreaExtractor.extractMinimalCycle(): Next point in sequence is " + currentVertex.position().toString());
			sequence.add(currentVertex);
			visited.add(currentVertex);
			nextVertex = getCounterclockwiseMost(previousVertex, currentVertex);
			previousVertex = currentVertex;
			currentVertex = nextVertex;
		}

		if (currentVertex == null)
		{
			// Filament found, not necessarily rooted at v0.
			extractFilament(previousVertex, firstAdjacentNode(previousVertex));
		}
		else if (currentVertex == current)
		{
			// Minimal cycle found.
			Polygon minimalCycle = new Polygon();
			List<Intersection> correspondingIntersections = new ArrayList<Intersection>();

			int i = 0;
			for (Intersection nodeInCycle : sequence)
			{
				minimalCycle.addVertex(nodeInCycle.position());
				correspondingIntersections.add(nodeInCycle);

				/* test if we're second-to-last */
				Intersection nextNodeInCycle;
				if (i < sequence.size() - 1) {
					nextNodeInCycle = sequence.get(i++);
					if (i == sequence.size() - 1) {
						nextNodeInCycle = sequence.get(0);
					}
				} else {
					nextNodeInCycle = sequence.get(0);
				}

				markCycleEdge(nodeInCycle, nextNodeInCycle);

			}

			logger.debug("AreaExtractor::extractMinimalCycle(): Storing minimal cycle.");
			if (substractRoadWidthFromAreas)
			{
				List<Double> distances = getSubtractDistance(correspondingIntersections);
				Polygon boundaries = new Polygon(minimalCycle);
				minimalizeCycle(minimalCycle, distances);
				subtractRoadWidths(minimalCycle, distances);

				// Discard wrong blocks
				if (minimalCycle.isSubAreaOf(boundaries))
				{
					cycles.add(minimalCycle);
				}
				// cycles.add(minimalCycle);
			}
			else
			{
				cycles.add(minimalCycle);
			}

			removeEdge(current, next);

			if (numberOfAdjacentNodes(current) == 1)
			{
				// Remove the filament rooted at v0.
				extractFilament(current, firstAdjacentNode(current));
			}
			if (numberOfAdjacentNodes(next) == 1)
			{
				// Remove the filament rooted at v1.
				extractFilament(next, firstAdjacentNode(next));
			}
		}
		else // vcurr was visited earlier
		{
			// A cycle has been found, but is not guaranteed to be a minimal
			// cycle. This implies v0 is part of a filament. Locate the
			// starting point for the filament by traversing from v0 away
			// from the initial v1.
			while (numberOfAdjacentNodes(current) == 2)
			{
				List<Intersection> neighbors = getAdjacentNodes(current);
				if (neighbors.get(0) != next)
				{
					next.set(current);
					current = neighbors.get(0);
				}
				else
				{
					next.set(current);
					current = neighbors.get(neighbors.size() - 1);
				}
			}
			extractFilament(current, next);
		}
	}

	private Intersection getClockwiseMost(Intersection previous, Intersection current) {

		List<Intersection> neighbors = getAdjacentNodes(current);

		Vector3d vCurrent;
		if (previous != null) {
			vCurrent = new Vector3d(previous.position().x - current.position().x, previous.position().y - current.position().y, 0);
		} else {
			vCurrent = new Vector3d(0, -1, 0);
		}
		Vector3d vNext = null; // Not initialized originally, since it gets
								// assigned the first time through the loop.
		Intersection next = null;
		boolean vCurrentIsConvex = false;

		Intersection adjacent = null;
		Vector3d vAdjacent = new Vector3d(0, 0, 0);
		for (Intersection adjacentNode : neighbors)
		{
			adjacent = adjacentNode;

			if (adjacent == previous)
			{
				continue;
			}

			vAdjacent.set(current.position());
			vAdjacent.sub(adjacent.position());

			if (next == null)
			{
				next = adjacent;
				vNext = vAdjacent;
				vCurrentIsConvex = (MathUtil.perpDotProduct(vNext, vCurrent) <= -Units.EPSILON); // perpDotProduct
				continue;
			}

			if (vCurrentIsConvex)
			{
				if (MathUtil.perpDotProduct(vCurrent, vAdjacent) < 0 || MathUtil.perpDotProduct(vNext, vAdjacent) < 0)
				{
					next = adjacent;
					vNext = vAdjacent;
					vCurrentIsConvex = MathUtil.perpDotProduct(vNext, vCurrent) <= -Units.EPSILON;
				}
			}
			else
			{
				if (MathUtil.perpDotProduct(vCurrent, vAdjacent) < 0 && MathUtil.perpDotProduct(vNext, vAdjacent) < 0)
				{
					next = adjacent;
					vNext = vAdjacent;
					vCurrentIsConvex = MathUtil.perpDotProduct(vNext, vCurrent) <= -Units.EPSILON;
				}
			}
		}

		return next;
	}

	private Intersection getCounterclockwiseMost(Intersection previous, Intersection current) { // BYREF
																								// current

		List<Intersection> neighbors = getAdjacentNodes(current);

		// Vector3d vCurrent = previous ? Vector(current.position(),
		// previous.position()) : Vector(0, -1);

		Vector3d vCurrent = (previous != null
				? new Vector3d(previous.position().x - current.position().x, previous.position().y - current.position().y, 0)
				: new Vector3d(0, -1, 0));

		Vector3d vNext = null;// Not initialized originally, since it gets
								// assigned the first time through the loop.
		Intersection next = null;
		boolean vCurrentIsConvex = false;

		for (Intersection adjacentNode : neighbors)
		{
			Intersection adjacent = adjacentNode;
			if (adjacent == previous) // pointer equality
			{
				continue;
			}

			Vector3d vAdjacent = new Vector3d(current.position().x - adjacent.position().x, current.position().y - adjacent.position().y, current.position().z - adjacent.position().z);

			if (next == null)
			{
				next = adjacent;
				vNext = vAdjacent;
				vCurrentIsConvex = (MathUtil.perpDotProduct(vNext, vCurrent) <= -Units.EPSILON);
				continue;
			}

			// if the current "next" is already convex.
			if (vCurrentIsConvex)
			{
				if (MathUtil.perpDotProduct(vCurrent, vAdjacent) > 0 && MathUtil.perpDotProduct(vNext, vAdjacent) > 0)
				{
					next = adjacent;
					vNext = vAdjacent;
					vCurrentIsConvex = MathUtil.perpDotProduct(vNext, vCurrent) <= -Units.EPSILON;
				}
			}
			else
			{
				if (MathUtil.perpDotProduct(vCurrent, vAdjacent) > 0 || MathUtil.perpDotProduct(vNext, vAdjacent) > 0)
				{
					next = adjacent;
					vNext = vAdjacent;
					vCurrentIsConvex = MathUtil.perpDotProduct(vNext, vCurrent) <= -Units.EPSILON;
				}
			}
		}

		return next;
	}

	/* Adjacent nodes access methods. */
	private int numberOfAdjacentNodes(Intersection node) {
		List<Intersection> l = getAdjacentNodes(node);
		return l.size();
	}

	/**
	 * Return all Adjacent intersections, or an empty list if none.
	 * 
	 * @param node
	 * @return
	 */
	private List<Intersection> getAdjacentNodes(Intersection node) {
		List<Intersection> retval = adjacentNodes.get(node);
		
		//Integrity check.
		if(retval==null || retval.size()==0){
			//no neighbors?
			if(node.getRoads().size()>0){
				throw new IllegalStateException("Node " + node.toString() + " has "+node.getRoads().size() + " roads, but AdjacentNodes claims zero.");
			}
		} else if (retval.size()!=node.getRoads().size()){
			throw new IllegalStateException("Node " + node.toString() + " has "+node.getRoads().size() + " roads, but AdjacentNodes claims " + retval.size()+".");
		}
		// FIXME exception when not in graph
		return retval == null ? new ArrayList<Intersection>() : retval;
	}

	private Intersection firstAdjacentNode(Intersection node) {
		// FIXME throw exception when empty. This means we have a floating
		// intersection, with no neighbors?
		logger.debug("firstAdjacentNode");
		if (numberOfAdjacentNodes(node) <= 0) {
			throw new IllegalStateException("Orphan Intersection Node " + node.toString());
		}
		return getAdjacentNodes(node).get(0);
	}

	private void initialize() {
		vertices = new ArrayList<Intersection>();
		adjacentNodes = new HashMap<Intersection, List<Intersection>>();
		cycleEdges = new HashSet<Pair<Intersection, Intersection>>();
		cycles = new ArrayList<Polygon>();

		reset();
	}

	// FIXME: Unnecessary?
	private void reset() {
		vertices.clear();
		adjacentNodes.clear();
		cycleEdges.clear();
		cycles.clear();
		substractRoadWidthFromAreas = false;
	}

	/**
	 * Utility function to dump adjacency lists (for debugging)
	 */
	private void dumpAdjacencyLists() {
		for (Entry<Intersection, List<Intersection>> e : adjacentNodes.entrySet()) {
			logger.debug(e.getKey().position().toString() + " Has this adjacent nodes :");
			for (Intersection i : e.getValue()) {
				logger.debug("\t" + i.position().toString());
			}
		}
	}

	private void dumpAdjacenciesFromVertices() {
		for (Intersection nodeIterator : vertices) {
			logger.debug(nodeIterator.position().toString() + " Has this adjacent nodes :");
			for (Intersection adjacentNodeIterator : nodeIterator.adjacentIntersections()) {
				logger.debug("\t" + adjacentNodeIterator.position().toString());
			}
		}
	}

	public AreaExtractor() {
		initialize();
	}

	// public AreaExtractor(StreetGraph streetGraph) {
	// this();
	// this.map = streetGraph;
	// }

	public AreaExtractor(AreaExtractor source) {
		initialize();

		vertices.addAll(source.vertices);
		adjacentNodes.putAll(source.adjacentNodes);
		cycleEdges.addAll(source.cycleEdges);
		cycles.addAll(source.cycles);
	}

	/**
	 * Set the width to use for a given {@link RoadType}.
	 * 
	 * @param type
	 *            the RoadType to set the width for.
	 * @param width
	 *            the width for the given RoadType.
	 * @deprecated use {@link RoadType#setWidth(width)} instead.
	 */
	public void setRoadWidth(RoadType type, double width) {
		type.setWidth(width);
		// roadWidths.put(type,width);
	}

	/**
	 * Set the width to use for a number of given {@link RoadType}s.
	 * 
	 * @deprecated use {@link RoadType#setWidth(width)} instead.
	 */
	public void setRoadWidths(Map<RoadType, Double> widths) {
		// roadWidths = widths;
		for (Map.Entry<RoadType, Double> e : widths.entrySet()) {
			e.getKey().setWidth(e.getValue());
		}
	}

	public List<Zone> extractZones(StreetGraph fromMap, Zone zoneConstraints) {

		reset();
		map = fromMap;
		copyVertices(map, zoneConstraints);
		substractRoadWidthFromAreas = false;

		getMinimalCycles();

		List<Zone> zones = new ArrayList<Zone>();
		for (Polygon foundZone : cycles)
		{
			Zone newZone = new Zone(map);
			newZone.setAreaConstraints(foundZone);
			zones.add(newZone);
		}

		return zones;
	}

	public List<Block> extractBlocks(StreetGraph fromMap, Zone zoneConstraints) {

		reset();
		this.map = fromMap;
		copyVertices(map, zoneConstraints);
		substractRoadWidthFromAreas = true;
		List<Block> blocks = new ArrayList<Block>();

		getMinimalCycles(); // FIXME: <-- Bug here.

		for (Polygon foundZone : cycles)
		{
			Block newBlock = new Block(zoneConstraints);
			newBlock.setAreaConstraints(foundZone);
			blocks.add(newBlock);
		}
		return blocks;
	}

}