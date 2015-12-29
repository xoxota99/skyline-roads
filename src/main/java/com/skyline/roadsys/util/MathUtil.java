package com.skyline.roadsys.util;

import java.util.*;

import javax.vecmath.*;

public class MathUtil {
	public static long seed = 0L;
	public static Random random = new Random(seed);

	/**
	 * 2D Perpendicular Dot Product. This is similar to a regular Dot Product,
	 * where v1 is rotated 90 degrees to the left.
	 * 
	 * Note: perpDotProduct is NOT commutative. In fact, perpDotProduct(a,b)
	 * will give the negative of perpDotProduct(b,a)
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double perpDotProduct(Vector3d v1, Vector3d v2) {
		return (v1.x * v2.y) - (v2.x * v1.y);
	}

	// Return an integer between min and max, inclusive.
	public static int randomInt(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	public static void initRandom(long seed) {
		MathUtil.seed = seed;
		MathUtil.random = new Random(seed);
	}

	/**
	 * Rotate a vector, in place, around the Z axis by a given angle.
	 * 
	 * @param v
	 * @param degrees
	 */
	public static void rotateAroundZ(Tuple3d v, double degrees) {

		double radians = degrees * (Math.PI / 180.0);
		double newX, newY;

		newX = v.x * Math.cos(radians) - v.y * Math.sin(radians);
		newY = v.x * Math.sin(radians) + v.y * Math.cos(radians);

		v.x = newX;
		v.y = newY;
	}

	/**
	 * Rotate a vector, in place, around the X axis by a given angle.
	 * 
	 * @param v
	 * @param degrees
	 */
	public static void rotateAroundX(Tuple3d v, double degrees) {

		double radians = degrees * (Math.PI / 180.0);
		double newY, newZ;

		newY = v.y * Math.cos(radians) - v.z * Math.sin(radians);
		newZ = v.y * Math.sin(radians) + v.z * Math.cos(radians);

		v.y = newY;
		v.z = newZ;
	}

	/**
	 * Rotate a vector, in place, around the Y axis by a given angle.
	 * 
	 * @param v
	 * @param degrees
	 */
	public static void rotateAroundY(Tuple3d v, double degrees) {
		double radians = degrees * (Math.PI / 180.0);
		double newX, newZ;

		newX = v.z * Math.sin(radians) + v.x * Math.cos(radians);
		newZ = v.z * Math.cos(radians) - v.x * Math.sin(radians);

		v.x = newX;
		v.z = newZ;
	}
}
