package com.livingobjects.ranges.services.launch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import com.livingobjects.ranges.services.RangesMatcher;

public class MatcherRunnable implements Runnable {

	private RangesMatcher rangesMatcher;
	private Integer item;
	private List<Integer> operations;
	private Semaphore sema;
	private List<List<String>> result;

	@Override
	/**
	 * Function that manage the "Thread launching"
	 */
	public void run() {
		result = new ArrayList<>();
		try {
			// This mode of launching supposed to have item or operations initialized
			if (item == null && operations == null)
				throw new IllegalStateException("Item and operations are not initialized");

			// if on "single operation" mode, directly call marchingLabels
			if (item != null) {
				result.add(rangesMatcher.marchingLabels(item));

			} else if (operations != null) {
				// if on "multiple operations" mode, launch marchingLabels on each item
				// contained in Operations
				result = operations.stream().map(item -> rangesMatcher.marchingLabels(item))
						.collect(Collectors.toList());
			}

		} catch (Exception e) {
			System.err.println("Error during thread run : " + e.getMessage());

		} finally {
			// When call is done, release the semaphore if exists
			if (sema != null) {
				sema.release();
			}
		}


	}

	public List<List<String>> getResult() {
		return result;
	}

	public void setRangesMatcher(RangesMatcher rm) {
		this.rangesMatcher = rm;
	}

	public void setItem(int item) {
		this.item = item;
	}

	public void setOperations(List<Integer> ope) {
		this.operations = ope;

	}

	public void setSemaphore(Semaphore sema) {
		this.sema = sema;

	}
}
