package com.livingobjects.ranges.models;

import lombok.Data;

@Data
public class Ranges2 {

	private String name;
	private Integer lowerBound;
	private Integer higherBound;

	public Ranges2(String name, Integer lowerBound, Integer higherBound) {
		super();
		this.name = name;
		this.lowerBound = lowerBound;
		this.higherBound = higherBound;
	}

	public boolean contains(int item) {
		return item >= lowerBound && item < higherBound;
	}

	@Override
	public String toString() {
		return name + " -> {" + lowerBound + ", " + higherBound + "}";
	}
}
