package com.livingobjects.ranges.services;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.livingobjects.ranges.models.Ranges;
import com.livingobjects.ranges.services.launch.MatcherRunnable;
import com.livingobjects.ranges.services.tools.RangesBuilder;

@RunWith(JUnit4.class)
public class RangesMatcherTest {

	private static Set<Ranges> allRanges;
	private static List<Integer> items;

	@BeforeClass
	public static void init() {
		Ranges r1 = new Ranges("A", 1, 5);
		Ranges r2 = new Ranges("B", 3, 8);
		Ranges r3 = new Ranges("C", 5, 10);

		allRanges = new HashSet<>();
		allRanges.add(r1);
		allRanges.add(r2);
		allRanges.add(r3);

		items = new ArrayList<>();
		items.add(1);
		items.add(4);
		items.add(5);
		items.add(8);
		items.add(11);

	}

	@Test(expected = IllegalStateException.class)
	public void testMarchinLabelsException() {

		RangesMatcher ms = new RangesMatcher(null);
		ms.marchingLabels(0);

	}

	@Test
	public void testMarchingLabels() {
		RangesMatcher.resetRangesSet();
		RangesMatcher rm = new RangesMatcher(allRanges);

		List<String> marchingLabels = rm.marchingLabels(1);
		assertEquals("(A)", RangesBuilder.formatStringList(marchingLabels));

		marchingLabels = rm.marchingLabels(4);
		assertEquals("(A,B)", RangesBuilder.formatStringList(marchingLabels));

		marchingLabels = rm.marchingLabels(5);
		assertEquals("(B,C)", RangesBuilder.formatStringList(marchingLabels));

		marchingLabels = rm.marchingLabels(8);
		assertEquals("(C)", RangesBuilder.formatStringList(marchingLabels));

		marchingLabels = rm.marchingLabels(11);
		assertEquals("()", RangesBuilder.formatStringList(marchingLabels));
	}

	// Thread Exception can't be tested with JUnit
/*	@Test(expected = IllegalStateException.class)
	public void testRunException() throws InterruptedException {
		RangesMatcher rm = new RangesMatcher(allRanges);

		Thread t = new Thread(rm);
		t.start();
		t.join();
	}
*/
	
	@Test
	public void testRunItem() throws InterruptedException {
		RangesMatcher rm = new RangesMatcher(allRanges);
		MatcherRunnable launcher = new MatcherRunnable();
		launcher.setItem(1);
		launcher.setRangesMatcher(rm);

		Thread t = new Thread(launcher);
		t.start();
		t.join();
	}

	@Test
	public void testRunItems() throws InterruptedException {
		RangesMatcher rm = new RangesMatcher(allRanges);
		MatcherRunnable launcher = new MatcherRunnable();
		launcher.setOperations(items);
		launcher.setRangesMatcher(rm);

		Thread t = new Thread(launcher);
		t.start();
		t.join();
	}

}

