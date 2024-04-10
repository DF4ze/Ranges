package com.livingobjects.ranges.services.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.livingobjects.ranges.models.Ranges2;

public class RangesBuilder {

	/**
	 * Function that generate severals {@link Ranges2}<br/>
	 * The number of {@link Ranges2} generated depends on param "number"
	 * 
	 * @param number Number of {@link Ranges2} that will be generated
	 * 
	 * @return the list of generated {@link Ranges2}
	 */
	public static List<Ranges2> generateRanges(int number, int minRange, int maxRange) {
		if (minRange > maxRange) {
			throw new IllegalStateException("Invalid bounds: minRange > maxRange");
		}

		List<Ranges2> rangesList = new ArrayList<>();

		for (int i = 0; i < number; i++) {
			int minBound = ThreadLocalRandom.current().nextInt(minRange, maxRange + 1);
			int maxBound = ThreadLocalRandom.current().nextInt(minBound, maxRange + 1);

			rangesList.add(new Ranges2(RangesBuilder.generateRangesName(), minBound, maxBound));
		}

		return rangesList;
	}

	/**
	 * Generate a list of pseudorandom integer
	 * 
	 * @param number   The number of item in the list
	 * @param minRange The minimum value of the Integer added
	 * @param maxRange The maximum value of the Integer added
	 * 
	 * @return List of Integer
	 */
	public static List<Integer> generateItems(int number, int minRange, int maxRange) {
		List<Integer> items = new ArrayList<>();

		for (int i = 0; i < number; i++) {
			items.add(ThreadLocalRandom.current().nextInt(minRange, maxRange + 1));
		}

		return items;
	}

	/**
	 * 
	 * Function that does an increment of the given param label<br/>
	 * This increment is done from 'A' to 'Z'<br/>
	 * If the increment overflow 'Z', a new letter is added to the returned
	 * String<br/>
	 * examples : <br/>
	 * - "A" returns "B"<br/>
	 * - "Z" returns "ZA"<br/>
	 * 
	 * @deprecated due to a too long name that overload application when working
	 *             with large bench.
	 * 
	 * 
	 * @param label based String used to be incremented
	 * 
	 * @return String with its last char incremented
	 */
	@Deprecated
	public static String generateLetterFrom(String label) {
		char lastChar = label.charAt(label.length() - 1);

		if (lastChar == 'Z') {
			return label + 'A';
		} else {
			return label.substring(0, label.length() - 1) + (char) (lastChar + 1);
		}
	}

	/**
	 * To avoid too long name generation, this function generate a random name from
	 * Ranges<br/>
	 * Basing on minBound, maxBound and its position in the Ranges list
	 * 
	 * 
	 * @return Random string qualifying name of the Ranges
	 */
	public static String generateRangesName() {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		int targetStringLength = 10;
		Random random = new Random();

		String generatedString = random.ints(leftLimit, rightLimit + 1).limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

		return generatedString;
	}

	public static String formatStringList(List<String> list) {
		// @formatter:off
		return list.stream()
				.map(n -> n.toString())
				.collect(Collectors.joining(",", "(", ")"));
		// @formatter:on
	}
}
