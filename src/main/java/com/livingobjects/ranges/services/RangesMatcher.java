package com.livingobjects.ranges.services;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.livingobjects.ranges.models.Ranges;

/**
 * Represent the unique access for the list of Ranges
 */
public class RangesMatcher {

	private static List<Ranges> rangesList;


	/**
	 * Force initialization with a list of Ranges
	 */
	public RangesMatcher(List<Ranges> list) {
		if (list == null || list.isEmpty()) {
			throw new IllegalStateException("Given list is empty or null");
		}

		if (rangesList == null) {
			rangesList = Collections.unmodifiableList(list);
		}
	}


	/**
	 * Referring to rangesList, this function return the label of each Ranges that
	 * contains the given Item
	 * 
	 * @param item Integer to be contained in Ranges
	 * @return List of labels that define Ranges that contains Item
	 */
	public List<String> marchingLabels(int item) {

		// @formatter:off
		return rangesList.stream()
 				.filter(r -> r.contains(item))
 				.map(Ranges::getName)
				.collect(Collectors.toList());
		// @formatter:on
	}

	/**
	 * Needed for JUnit tests
	 */
	protected static void resetRangesList() {
		rangesList = null;
	}

}
