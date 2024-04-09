package com.livingobjects.ranges.models;

import lombok.Data;

@Data
public class BenchResult {

	private BenchMode benchMode;

	private Integer lowerBound;
	private Integer higherBound;
	private Integer rangeNumber;
	private Integer maxIteration;

	private Integer maxNbThreads;
	private Integer nbOpeByThread;

	private String duration;

	@Override
	public String toString() {
		return duration + " [benchMode=" + benchMode + ", maxNbThreads=" + maxNbThreads + ", nbOpeByThread="
				+ nbOpeByThread + "]";
	}

}
