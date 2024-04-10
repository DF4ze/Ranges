package com.livingobjects.ranges.models;


public record BenchResult(BenchMode benchMode, Integer lowerBound, Integer higherBound, Integer rangeNumber,
		Integer maxIteration, Integer maxNbThreads, Integer nbOpeByThread, String duration) {

	@Override
	public String toString() {
		return duration + " [benchMode=" + benchMode + ", maxNbThreads=" + maxNbThreads + ", nbOpeByThread="
				+ nbOpeByThread + "]";
	}
}
