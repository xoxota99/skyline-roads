package com.skyline.roadsys.streetgraph;

/*
 * Shamelessly stolen from https://github.com/pazdera/libcity
 */

//TODO: Refactor this. IntersectionType should be a class, Intersection containing
	// IntersectionType (OVERLAPPING, INTERSECTING, etc) and intersectionPoint.
	// Then remove the BYREF / output param. Output params are an anti-pattern
	// in Java.
public enum IntersectionType {
	INTERSECTING, // =0
	ORTHOGONAL,
	PARALLEL,
	IDENTICAL,
	CONTAINING,
	CONTAINED,
	OVERLAPPING	
}
