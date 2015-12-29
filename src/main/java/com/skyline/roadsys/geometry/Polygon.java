package com.skyline.roadsys.geometry;

import java.util.*;

import javax.vecmath.*;

import com.skyline.roadsys.streetgraph.*;
import com.skyline.roadsys.util.*;

/**
 * 2D Polygon representation
 * 
 * @author philippd
 * 
 */
/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */
public class Polygon {

	private List<Point> vertices;

	/**
	 * Empty Polygon
	 */
	public Polygon() {
		initialize();
	}

	/**
	 * Polygon with multiple points.
	 */
	public Polygon(Point... points) {
//		if (points.length < 3) {
//			throw new IllegalArgumentException("Polygon must consist of three or more points!");
//		}

		initialize();
		for (Point pt : points) {
			addVertex(pt);
		}
	}

	public Polygon(Polygon source) {
		initialize();

		for (Point pt : source.vertices) {
			addVertex(new Point(pt));
		}
	}

	public Point vertex(int index) {
		return vertices.get(index);
	}

	public LineSegment edge(int index) {
		return new LineSegment(vertices.get(index), vertices.get((index + 1) % numberOfVertices()));
	}

	public int numberOfVertices() {
		return vertices.size();
	}

	public void addVertex(Point vertex) {

		/* To avoid zero length edges. */
		if (numberOfVertices() > 0)
		{

			if (vertex.distance(vertices.get(vertices.size() - 1)) <= Units.COORDINATES_EPSILON)
			{
				return;
			}
		}

		vertices.add(new Point(vertex));

		// FIXME: check if the vertex is in a plane with other vertices!
		// (possibly set Z to zero?)
	}

	/**
	 * Update an existing vertex IN PLACE.
	 * 
	 * @param index
	 *            - 0-based index of the vertex to update
	 * @param vertex
	 *            - The replacement vertex.
	 */
	public void updateVertex(int index, Point vertex) {
		assert (index < vertices.size());
		vertices.get(index).set(vertex); // update in place
		// vertices.set(index, vertex); //replace.
	}

	public void removeVertex(int index) {
		if (index >= vertices.size())
		{
			throw new IndexOutOfBoundsException("Tried to remove vertex " + index + " from a polygon with only " + vertices.size() + " vertices.");
		}
		vertices.remove(index);
	}

	public void clear() {
		vertices.clear();
	}

	/** Works ONLY in 2D !!! */
	public double area() {
		return Math.abs(signedArea());
	}

	/** Works ONLY in 2D !!! */
	public Point centroid() {
		double area = 0, areaStep = 0;
		double x = 0, y = 0;

		int currentVertexPosition = 0, count = numberOfVertices();
		Point currentVertex = null, nextVertex = null;

		for (currentVertexPosition = 0; currentVertexPosition < count; currentVertexPosition++)
		{
			currentVertex = vertices.get(currentVertexPosition);
			nextVertex = vertices.get((currentVertexPosition + 1) % count);

			areaStep = currentVertex.x * nextVertex.y - nextVertex.x * currentVertex.y;
			area += areaStep;

			x += (currentVertex.x + nextVertex.x) * areaStep;
			y += (currentVertex.y + nextVertex.y) * areaStep;
		}

		Point centroid = new Point(x / (6 * (area / 2)), y / (6 * (area / 2)), 0);
		return centroid;
	}

	/**
	 * Get normal vector of a certain edge. The vector ALWAYS points inside the
	 * polygon.
	 * 
	 * @param[in] edgeNumber Number of the edge. Edges are numbered from 0 to
	 *            numberOfVertices() - 1.
	 * @return Normalized normal vector.
	 */
	public Vector3d edgeNormal(int edgeNumber) {
		int verticesNumber = numberOfVertices();
		assert (edgeNumber < verticesNumber);

		int first = edgeNumber;
		int second = (edgeNumber + 1) % verticesNumber;

		Point fp = vertices.get(first);
		Point sp = vertices.get(second);

		Vector3d direction = new Vector3d(sp), normalVector = new Vector3d();
		direction.sub(fp);

		normalVector.cross(direction, normal());
		normalVector.normalize();

		// normalVector.negate(); //testing a theory.

		Point edgeCenter = new Point((fp.x + sp.x) / 2,
				(fp.y + sp.y) / 2,
				(fp.z + sp.z) / 2);

		Ray testRay = new Ray(edgeCenter, normalVector);

		Point intersection = new Point();

		int intersections = 0, vertexHits = 0;

		for (int i = 0; i < verticesNumber; i++)
		{
			//for every edge.
			LineSegment currentEdge = edge(i);
			//Does the normal of the edge we're testing intersect with this edge?
			if (i != edgeNumber && testRay.intersection2D(currentEdge, intersection) == IntersectionType.INTERSECTING)
			{
				if (intersection.equals(currentEdge.begining()) || intersection.equals(currentEdge.end()))
				{
					vertexHits++;
				}
				intersections++;	
			}
		}

		assert ((vertexHits % 2) == 0);
		intersections -= vertexHits / 2;

		if (intersections % 2 == 0)
			normalVector.negate(); 
		return normalVector;
	}

	/**
	 * Get normal vector to the plane this polygon makes.
	 * 
	 * @return Normalized normal vector.
	 */
	public Vector3d normal() {

		assert (numberOfVertices() >= 3);

		Vector3d first = new Vector3d(vertices.get(0)), second = new Vector3d(), invSecond = new Vector3d();
		first.sub(vertices.get(1));

		first.normalize();

		int current, next, verticesCount = numberOfVertices();
		for (int i = 1; i < verticesCount; i++)
		{
			current = i;
			next = (i + 1) % verticesCount;
			second.set(vertices.get(next));
			second.sub(vertices.get(current));
			second.normalize();

			invSecond.negate(second);

			/* Edges are not parallel */
			if (!first.equals(second) && !first.equals(invSecond))
			{
				Vector3d normalVector = new Vector3d();
				normalVector.cross(first, second);
				normalVector.normalize();
				return normalVector;
			}
		}

		assert ("HERE should be exception" == null);
		return new Vector3d(1, 0, 0);
	}

	/**
	 * Rotate polygon around its centroid by amount of degrees specified by the
	 * parameters.
	 * 
	 * @remarks Uses centroid method, so it's not safe when the polygon is not
	 *          in the XY plane.
	 * @note Positive amount of degrees is rotation counterclock-wise. Negative
	 *       is clockwise.
	 * @param[in] xDegrees Amount of degrees to rotate in X.
	 * @param[in] yDegrees Amount of degrees to rotate in Y.
	 * @param[in] zDegrees Amount of degrees to rotate in Z.
	 */
	public void rotate(double xDegrees, double yDegrees, double zDegrees)
	{
		Point center = centroid();

		Point centroidToVertex = new Point();
		for (int i = 0; i < numberOfVertices(); i++)
		{
			centroidToVertex.set(vertex(i));
			centroidToVertex.sub(center);
			MathUtil.rotateAroundX(centroidToVertex, xDegrees);
			MathUtil.rotateAroundY(centroidToVertex, yDegrees);
			MathUtil.rotateAroundZ(centroidToVertex, zDegrees);
			centroidToVertex.add(center);
			updateVertex(i, centroidToVertex);
		}
	}

	/**
	 * Scale polygon by specified factor.
	 * 
	 * @note The expansion is computed from polygon's centroid.
	 * @param[in] factor The scale factor. Passing number <= 0 will result in
	 *            unspecified behavior.
	 */
	public void scale(double factor) {
		assert (factor > 0);
		Point center = centroid();

		Point centroidToVertex = new Point();
		for (int i = 0; i < numberOfVertices(); i++)
		{
			centroidToVertex.set(vertex(i));
			centroidToVertex.sub(center);
			centroidToVertex.scale(factor);
			centroidToVertex.add(center);
			updateVertex(i, centroidToVertex);
		}
	}

	/**
	 * "Shrink" the polygon by the given distance. Contracts all vertices toward the polygon centroid.
	 * 
	 * @param[in] distance Distance to be substracted
	 */
	public void subtract(double distance) {
		int previous, current, next;
		int verticesCount = numberOfVertices();
		Line first = new Line(), second = new Line();
		Vector3d firstNormal, secondNormal;
		IntersectionType result;
		Point newVertex = new Point();
		Polygon oldArea = new Polygon(this);

		for (int i = 0; i < verticesCount; i++)
		{
			previous = (i - 1) < 0 ? verticesCount - 1 : i - 1;
			current = i;
			next = (i + 1) % verticesCount;

			firstNormal = oldArea.edgeNormal(previous);
			firstNormal.normalize();
			Vector3d fn = new Vector3d(firstNormal);
			fn.scale(distance);

			Point v = new Point(oldArea.vertex(previous));
			v.add(fn);
			first.setBegining(v);

			v = new Point(oldArea.vertex(current));
			v.add(fn);
			first.setEnd(v);

			secondNormal = oldArea.edgeNormal(current);
			secondNormal.normalize();
			Vector3d sn = new Vector3d(secondNormal);
			sn.scale(distance);

			v = new Point(oldArea.vertex(current));
			v.add(sn);
			second.setBegining(v);

			v = new Point(oldArea.vertex(next));
			v.add(sn);
			second.setEnd(v);

			result = first.intersection2D(second, newVertex); // BYREF newVertex
			if (result == IntersectionType.PARALLEL)
			{
				newVertex = new Point(oldArea.vertex(current));
				newVertex.add(fn);
			}

			updateVertex(current, newVertex);
		}
	}

	/**
	 * Substract distance just from a single edge.
	 * 
	 * @param[in] i Edge index
	 * @param[in] distance Distance to be substracted
	 */
	public void substractEdge(int edgeNumber, double distance)
	{
		assert (numberOfVertices() >= 3);

		int previous, current1, current2, next;
		int verticesCount = numberOfVertices();

		previous = (edgeNumber - 1) < 0 ? verticesCount - 1 : edgeNumber - 1;
		current1 = edgeNumber;
		current2 = (edgeNumber + 1) % verticesCount;
		next = (edgeNumber + 2) % verticesCount;

		Vector3d normal = edgeNormal(edgeNumber);
		normal.normalize();
		Vector3d normald = new Vector3d(normal);
		normald.scale(distance);
		Line first = new Line(vertex(previous), vertex(current1));
		Line second = new Line(vertex(next), vertex(current2));
		Point t = new Point(vertex(current1));
		t.add(normald);
		Point b = new Point(vertex(current2));
		b.add(normald);
		Line center = new Line(t, b);

		Point newVertex1 = new Point(), newVertex2 = new Point();
		IntersectionType result;

		result = first.intersection2D(center, newVertex1); // BYREF newVertex1
		if (result == IntersectionType.PARALLEL)
		{
			newVertex1 = vertex(current1);
			newVertex1.add(normald);
		}
		updateVertex(current1, newVertex1);

		result = second.intersection2D(center, newVertex2); // BYREF newVertex2
		if (result == IntersectionType.PARALLEL)
		{
			newVertex2 = vertex(current2);
			newVertex2.add(normald);
		}
		updateVertex(current2, newVertex2);

	}

	public List<Polygon> split(Line splitLine) {
		List<Point> vertexList = new ArrayList<Point>();
		List<Point> intersections = new ArrayList<Point>();

		LineSegment currentEdge;
		IntersectionType result;
		Point intersection = new Point();

		/* for each edge */
		for (int i = 0; i < numberOfVertices(); i++)
		{
			currentEdge = edge(i);

			vertexList.add(vertex(i));
			intersection = new Point();
			result = currentEdge.intersection2D(splitLine, intersection); // BYREF
																			// intersection
			if (result == IntersectionType.INTERSECTING)
			{
				if (!intersection.equals(currentEdge.begining()) &&
						!intersection.equals(currentEdge.end()))
				{
					vertexList.add(intersection);
					intersections.add(intersection);
				}
				else
				{
					intersections.add(intersection);
				}
			}
		}

		// sort / uniqueify. Ugh. How expensive is this?
		Set<Point> s = new TreeSet<Point>(intersections);
		intersections.clear();
		intersections.addAll(s);

		List<Polygon> stack = new ArrayList<Polygon>();
		List<Polygon> output = new ArrayList<Polygon>();
		Polygon top;

		/* Split line was out or was just touching a vertex */
		if (intersections.size() <= 1)
		{
			output.add(new Polygon(this));
			return output;
		}

		stack.add(new Polygon());
		for (Point vertexIterator : vertexList) {
			top = stack.get(stack.size() - 1);

			top.addVertex(vertexIterator);

			if (isVertexIntersection(vertexIterator, intersections))
			{
				if (top.numberOfVertices() > 0 && areVerticesInPair(vertexIterator, top.vertex(0), intersections))
				{
					if (top.isClosed())
					{
						output.add(top);
					}

					stack.remove(stack.size() - 1);
					top = stack.get(stack.size() - 1);
					top.addVertex(vertexIterator);
				}
				else
				{
					stack.add(new Polygon());
					top = stack.get(stack.size() - 1);
					top.addVertex(vertexIterator);
				}
			}
		}

		if (stack.size() > 0)
		{
			top = stack.get(stack.size() - 1);
			if (top.isClosed())
			{
				output.add(top);
			}
		}

		return output;

	}

	public boolean encloses2D(Point point)
	{
		int currentVertexPosition = 0, count = numberOfVertices();
		Point currentVertex = null, nextVertex = null;
		LineSegment currentLine = new LineSegment();

		boolean isInside = false;

		for (currentVertexPosition = 0; currentVertexPosition < count; currentVertexPosition++)
		{
			currentVertex = vertices.get(currentVertexPosition);
			nextVertex = vertices.get((currentVertexPosition + 1) % count);

			/*
			 * The algorithm is unreilable at the edges so we check them
			 * separately to make sure.
			 */
			currentLine.setBegining(currentVertex);
			currentLine.setEnd(nextVertex);
			if (currentLine.hasPoint2D(point))
			{
				return true;
			}

			if (((currentVertex.y > point.y) != (nextVertex.y > point.y)) &&
					(point.x < (nextVertex.x - currentVertex.x) / (nextVertex.y - currentVertex.y)
							* (point.y - currentVertex.y) + currentVertex.x))
			{
				isInside = !isInside;
			}
		}

		return isInside;

	}

	public boolean isNonSelfIntersecting() {
		List<Point> points = new ArrayList<Point>();
		List<Integer> sequence = new ArrayList<Integer>();

		return triangulation(points, sequence); // BYREF
	}

	public boolean isClosed() {
		return numberOfVertices() >= 3;
	}

	public List<Point> triangulate()
	{
		List<Point> points = new ArrayList<Point>();
		List<Integer> sequence = new ArrayList<Integer>();

		triangulation(points, sequence);

		return points;
	}

	public List<Integer> getSurfaceIndexes()
	{
		List<Point> points = new ArrayList<Point>();
		List<Integer> sequence = new ArrayList<Integer>();

		triangulation(points, sequence);

		return sequence;
	}

	public boolean isSubAreaOf(Polygon biggerPolygon) {
		for (Point p : vertices)
		{
			if (!biggerPolygon.encloses2D(p))
			{
				return false;
			}
		}

		return true;
	}

	public String toString() {
		String output = "Polygon(";

		for (int i = 0; i < numberOfVertices(); i++)
		{
			output += vertices.get(i).toString() + ", ";
		}
		return output + ").";
	}

	private void initialize() {
		vertices = new ArrayList<Point>();
	}

	private double signedArea() {
		{
			double area = 0;

			int currentVertexPosition = 0, count = numberOfVertices();
			Point currentVertex = null, nextVertex = null;

			for (currentVertexPosition = 0; currentVertexPosition < count; currentVertexPosition++)
			{
				currentVertex = vertices.get(currentVertexPosition);
				nextVertex = vertices.get((currentVertexPosition + 1) % count);

				area += currentVertex.x * nextVertex.y - currentVertex.y * nextVertex.x;
			}

			return area / 2;
		}
	}

	/* Helper functions for split polygon */
	private boolean isVertexIntersection(Point vertex, List<Point> intersections)
	{
		for (Point vertexIterator : intersections) {
			if (vertexIterator.equals(vertex))
			{
				return true;
			}
		}

		return false;
	}

	// FIXME: Heavily refactored. Test thoroughly.
	private boolean areVerticesInPair(Point first, Point second, List<Point> intersections)
	{
		for (int i = 0; i < intersections.size(); i++) {
			if (intersections.get(i).equals(first)) {
				return (i == 0 && intersections.get(intersections.size() - 1).equals(second))
						|| (i == intersections.size() - 1 && intersections.get(0).equals(second))
						|| (i % 2 == 0 && intersections.get((i + 1)).equals(second))
						|| (i % 2 == 1 && intersections.get(i - 1).equals(second));
			}
		}
		return false;
	}

	/* Polygon triangulation */
	/*
	 * Taken from:
	 * http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
	 */
	// triangulate a contour/polygon, places results in vector
	// as series of triangles.
	private boolean triangulation(List<Point> points, List<Integer> sequence) {
		/* allocate and initialize list of Vertices in polygon */
		assert (numberOfVertices() >= 3);

		int n = vertices.size();

		int[] V = new int[n];

		/* we want a counter-clockwise polygon in V */

		if (0.0 < signedArea())
			for (int v = 0; v < n; v++)
				V[v] = v;
		else
			for (int v = 0; v < n; v++)
				V[v] = (n - 1) - v;

		int nv = n;

		/* remove nv-2 Vertices, creating 1 triangle every time */
		int count = 2 * nv; /* error detection */

		for (int v = nv - 1; nv > 2;) // (int m = 0, v = nv - 1; nv > 2;)
		{
			/* if we loop, it is probably a non-simple polygon */
			if (0 >= (count--))
			{
				// ** Triangulate: ERROR - probable bad polygon!
				return false;
			}

			/* three consecutive vertices in current polygon, <u,v,w> */
			int u = v;
			if (nv <= u)
				u = 0; /* previous */
			v = u + 1;
			if (nv <= v)
				v = 0; /* new v */
			int w = v + 1;
			if (nv <= w)
				w = 0; /* next */

			if (snip(u, v, w, nv, V))
			{
				int a, b, c, s, t;

				/* true names of the vertices */
				a = V[u];
				b = V[v];
				c = V[w];

				/* output Triangle */
				points.add(vertices.get(a));
				points.add(vertices.get(b));
				points.add(vertices.get(c));

				sequence.add(a);
				sequence.add(b);
				sequence.add(c);

				// m++;

				/* remove v from remaining polygon */
				for (s = v, t = v + 1; t < nv; s++, t++)
					V[s] = V[t];
				nv--;

				/* reset error detection counter */
				count = 2 * nv;
			}
		}

		return true;
	}

	// decide if point Px/Py is inside triangle defined by
	// (Ax,Ay) (Bx,By) (Cx,Cy)
	private boolean isInsideTriangle(double Ax, double Ay,
			double Bx, double By,
			double Cx, double Cy,
			double Px, double Py)
	{
		double ax, ay, bx, by, cx, cy, apx, apy, bpx, bpy, cpx, cpy;
		double cCROSSap, bCROSScp, aCROSSbp;

		ax = Cx - Bx;
		ay = Cy - By;
		bx = Ax - Cx;
		by = Ay - Cy;
		cx = Bx - Ax;
		cy = By - Ay;
		apx = Px - Ax;
		apy = Py - Ay;
		bpx = Px - Bx;
		bpy = Py - By;
		cpx = Px - Cx;
		cpy = Py - Cy;

		aCROSSbp = ax * bpy - ay * bpx;
		cCROSSap = cx * apy - cy * apx;
		bCROSScp = bx * cpy - by * cpx;

		return ((aCROSSbp >= 0.0) && (bCROSScp >= 0.0) && (cCROSSap >= 0.0));
	}

	private boolean snip(int u, int v, int w, int n, int[] V) // BYREF V
	{
		int p;
		double Ax, Ay, Bx, By, Cx, Cy, Px, Py;

		Ax = vertices.get(V[u]).x;
		Ay = vertices.get(V[u]).y;

		Bx = vertices.get(V[v]).x;
		By = vertices.get(V[v]).y;

		Cx = vertices.get(V[w]).x;
		Cy = vertices.get(V[w]).y;

		if (Units.EPSILON > (((Bx - Ax) * (Cy - Ay)) - ((By - Ay) * (Cx - Ax))))
		{
			return false;
		}

		for (p = 0; p < n; p++)
		{
			if ((p == u) || (p == v) || (p == w))
				continue;
			Px = vertices.get(V[p]).x;
			Py = vertices.get(V[p]).y;
			if (isInsideTriangle(Ax, Ay, Bx, By, Cx, Cy, Px, Py))
				return false;
		}

		return true;
	}
}
