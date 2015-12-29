package com.skyline.roadsys.area;

import org.apache.commons.logging.*;

import com.skyline.roadsys.geometry.*;

/**
 * Area implementation using directed polygon graphs.
 * 
 * @author philippd
 * 
 */
public class SubRegion {

	Log logger = LogFactory.getLog(this.getClass());

	public static class Edge implements Comparable<Edge>
	{
		Point begining;
		double s;
		boolean hasRoadAccess;
		Edge previous;
		Edge next;

		@Override
		public int compareTo(Edge other) {
			return begining.compareTo(((Edge) other).begining);
		}
	}

	private Edge polygonGraph;

	public SubRegion() {
		initialize();
	}

	public SubRegion(Edge edge) {
		initialize();
		polygonGraph = copyPolygonGraph(edge);
	}

	public SubRegion(Polygon polygon) {
		initialize();
		polygonGraph = constructPolygonGraph(polygon);
	}

	public SubRegion(SubRegion source) {
		initialize();

		polygonGraph = copyPolygonGraph(source.polygonGraph);
	}

	public Edge getFirstEdge() {
		return polygonGraph;
	}

	public Edge insert(Edge after, Point begining) {
		if (polygonGraph == null)
		{
			return insertFirst(begining);
		}
		if (after == null) {
			throw new IllegalArgumentException("Argument 'after' cannot be null.");
		}

		Edge newEdge = new Edge();
		newEdge.begining = begining;
		newEdge.hasRoadAccess = false;
		newEdge.s = 0;

		Edge next = after.next;
		after.next = newEdge;
		newEdge.previous = after;
		newEdge.next = next;
		next.previous = newEdge;

		return newEdge;
	}

	public void bridge(Edge first, Edge second)
	{
		Edge otherFirst;
		Edge otherSecond;

		logger.debug("BRIDGING GRAPH:");
		logger.debug("  First  road access = " + first.hasRoadAccess);
		logger.debug("  Second road access = " + second.hasRoadAccess);
		otherFirst = insert(first, first.begining);
		otherFirst.hasRoadAccess = first.hasRoadAccess;

		otherSecond = insert(second, second.begining);
		otherSecond.hasRoadAccess = second.hasRoadAccess;

		first.next = otherSecond;
		otherSecond.previous = first;

		second.next = otherFirst;
		otherFirst.previous = second;

		logger.debug(" after:");
		logger.debug("  First  road access = " + first.hasRoadAccess);
		logger.debug("  Second road access = " + second.hasRoadAccess);

		first.hasRoadAccess = false;
		second.hasRoadAccess = false;
		// SubRegion::Edge current = first;
		// do
		// {
		// current = current.next;
		// } while (current != first);
		//
		// current = second;
		// do
		// {
		// current = current.next;
		// } while (current != second);
	}

	/**
	 * 
	 * @return true if any Edge in the graph has road access.
	 */
	public boolean hasRoadAccess() {
		Edge current = polygonGraph;

		do
		{
			if (current.hasRoadAccess)
			{
				return true;
			}
			current = current.next;
		} while (current != polygonGraph);

		return false;
	}

	public Edge getLongestEdgeWithRoadAccess() {
		if (polygonGraph == null) {
			throw new IllegalStateException("polygonGraph cannot be null.");
		}

		Edge current = polygonGraph;
		Edge longest = null;

		do
		{
			if (longest == null && current.hasRoadAccess)
			{
				longest = current;
			}

			if (current.hasRoadAccess &&
					current.begining.distance(current.next.begining) >
					longest.begining.distance(longest.next.begining))
			{
				longest = current;
			}
			current = current.next;
		} while (current != polygonGraph);

		return longest;
	}

	public Edge getLongestEdgeWithoutRoadAccess() {
		Edge current = polygonGraph;
		Edge longest = null;

		do
		{
			if (longest == null && !current.hasRoadAccess)
			{
				longest = current;
			}

			if ((!current.hasRoadAccess) &&
					current.begining.distance(current.next.begining) >
					longest.begining.distance(longest.next.begining))
			{
				longest = current;
			}
			current = current.next;
		} while (current != polygonGraph);

		return longest;
	}

	public Polygon toPolygon() {
		Edge current = polygonGraph;
		Polygon polygon = new Polygon();

		do
		{
			polygon.addVertex(current.begining);
			current = current.next;
		} while (current != polygonGraph);

		return polygon;
	}

	public String toString() {
		Edge current = polygonGraph;
		StringBuffer output = new StringBuffer();

		output.append("SubRegion( \n");
		do
		{
			output.append("  Edge(\n");
			output.append("    point      = ").append(current.begining.toString()).append("\n");
			output.append("    roadAccess = ").append(current.hasRoadAccess).append("\n");
			output.append("    s          = ").append(current.s).append("),\n");
			current = current.next;
		} while (current != polygonGraph);
		output.append(")\n");

		return output.toString();
	}

	private Edge insertFirst(Point begining) {
		polygonGraph = new Edge();
		polygonGraph.begining = begining;
		polygonGraph.hasRoadAccess = false;
		polygonGraph.s = 0;

		polygonGraph.previous = polygonGraph;
		polygonGraph.next = polygonGraph;

		return polygonGraph;
	}

	private Edge constructPolygonGraph(Polygon polygon) {
		if (polygon.numberOfVertices() < 3) {
			throw new IllegalArgumentException("Argument 'polygon' must contain >=3 vertices.");
		}

		Edge current = new Edge();
		Edge first = current;
		Edge next = null;
		// Edge previous = null;
		for (int i = 0; i < polygon.numberOfVertices(); i++)
		{
			current.begining = polygon.vertex(i);
			current.hasRoadAccess = false;

			next = new Edge();
			// previous = current;
			current.next = next;
			next.previous = current;

			current = next;
		}

		/* Connect the end to the begining */
		current = next.previous; // one step back

		current.next = first; // connect to cycle
		first.previous = current; // and back

		return first;
	}

	private Edge copyPolygonGraph(Edge source)
	{
		Edge sourceCurrent = source;

		Edge current = new Edge();
		Edge first = current;
		Edge next;
		// Edge previous = null;

		do
		{
			current.begining = sourceCurrent.begining;
			current.hasRoadAccess = sourceCurrent.hasRoadAccess; // All block
																	// edges
																	// have road
																	// access
			current.s = sourceCurrent.s;

			next = new Edge();
			// previous = current;
			current.next = next;
			next.previous = current;
			current = next;

			sourceCurrent = sourceCurrent.next;
		} while (sourceCurrent != source);

		/* Connect the end to the begining */
		current = next.previous; // one step back
		current.next = first; // connect to cycle
		first.previous = current; // and back

		return first;
	}

	private void initialize() {
		polygonGraph = null;
	}

}
