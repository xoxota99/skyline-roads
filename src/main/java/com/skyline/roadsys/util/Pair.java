package com.skyline.roadsys.util;

public class Pair<T, J> {
	public T first;
	public J second;

	public Pair(T first, J second) {
		this.first = first;
		this.second = second;
	}

	public boolean equals(Pair<T, J> p) {
		return (((p.first == null && first == null || p.first.equals(first)) && (p.second == null && second == null || p.second.equals(second))) || ((p.first == null && second == null || p.first.equals(second)) && (p.second == null && first == null || p.second.equals(first))));
	}
}
