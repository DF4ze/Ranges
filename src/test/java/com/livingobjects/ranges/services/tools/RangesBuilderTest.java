package com.livingobjects.ranges.services.tools;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.livingobjects.ranges.models.Ranges2;

@RunWith(JUnit4.class)
public class RangesBuilderTest {

	private static int nbIteration = 50; // Number of iteration on "generateRanges()"
	private static int lb = 10; // lowerBound
	private static int hb = 50; // higherbound

	@SuppressWarnings("deprecation")
	@Test
	public void testGenerateLetterFrom() {
		String gen1 = RangesBuilder.generateLetterFrom("A");
		String gen2 = RangesBuilder.generateLetterFrom("Z");
		String gen3 = RangesBuilder.generateLetterFrom("ZE");

		assertEquals("B", gen1);
		assertEquals("ZA", gen2);
		assertEquals("ZF", gen3);
	}

	@Test
	public void testGenerateRanges() {
		boolean exception = false;
		try {
			RangesBuilder.generateRanges(3, lb, hb);
		} catch (IllegalStateException e) {
			exception = true;
		}
		assertEquals(false, exception);
	}

	@Test(expected = IllegalStateException.class)
	public void testGenerateRangesException() {
		RangesBuilder.generateRanges(3, 40, 10);

	}

	@Test
	public void testGenerateRangesCount() {

		List<Ranges2> rangesList = RangesBuilder.generateRanges(nbIteration, lb, hb);

		assertEquals(nbIteration, rangesList.size());
	}

	@Test
	public void testGenerateRangesValues() {

		List<Ranges2> rangesList = RangesBuilder.generateRanges(nbIteration, lb, hb);
		for (Ranges2 ranges : rangesList) {
			assertEquals(true, ranges.getLowerBound() >= lb);
			assertEquals(true, ranges.getHigherBound() <= hb);
		}

		assertEquals(nbIteration, rangesList.size());
	}

	@Test
	public void testFormat() {
		List<String> list = new ArrayList<>();
		list.add("A");
		list.add("ZA");
		list.add("ZZA");

		String formatedString = RangesBuilder.formatStringList(list);

		assertEquals("(A,ZA,ZZA)", formatedString);
	}

	@Test
	public void testGenerateItemsCount() {
		List<Integer> generatedItems = RangesBuilder.generateItems(nbIteration, lb, hb);

		assertEquals(nbIteration, generatedItems.size());
	}

	@Test
	public void testGenerateItemsValues() {
		List<Integer> generatedItems = RangesBuilder.generateItems(nbIteration, lb, hb);

		for (Integer item : generatedItems) {
			assertEquals(true, item >= lb);
			assertEquals(true, item <= hb);
		}
	}
}
