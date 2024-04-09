package com.livingobjects.ranges.services.tools;

import java.time.Duration;
import java.time.Instant;

public class Chrono {

	private Instant start;

	public void pick() {
		start = Instant.now();
	}

	public String compare() {
		Instant finish = Instant.now();
		Duration duration = Duration.between(start, finish);

		long HH = duration.toHours();
		long MM = duration.toMinutesPart();
		long SS = duration.toSecondsPart();
		long mmm = duration.toMillisPart();
		String timeInHHMMSS = String.format("%02d:%02d:%02d %03d", HH, MM, SS, mmm);

		return timeInHHMMSS;
	}
}
