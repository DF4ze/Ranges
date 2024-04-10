package com.livingobjects.ranges.models;

public record Ranges(String name, int lowerBound, int higherBound) {

	public boolean contains(int item) {
		return item >= lowerBound && item < higherBound;
	}

	@Override
	public String toString() {
		return name + " -> {" + lowerBound + ", " + higherBound + "}";
	}
}
