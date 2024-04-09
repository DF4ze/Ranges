package com.livingobjects.ranges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.livingobjects.ranges.models.BenchMode;
import com.livingobjects.ranges.models.BenchResult;
import com.livingobjects.ranges.models.Ranges;
import com.livingobjects.ranges.services.RangesMatcher;
import com.livingobjects.ranges.services.launch.MatcherRunnable;
import com.livingobjects.ranges.services.tools.Chrono;
import com.livingobjects.ranges.services.tools.RangesBuilder;

/**
 * This class launches severals tests to see which implementation is the more
 * efficient. See attached ranges.pdf to see exercise
 * 
 * To run a 1 000 000 iterations test, we'll need to enlarge the heap space
 * adding "-Xmx8G" to our JVM configuration.
 * 
 * @author Clément-Aurélien ORTIZ 08/04/2024
 * 
 */
public class App {

	/*
	 * Globals variables to drive benchmark
	 */

	// @formatter:off
	private static int lowerBound = 1; 				// Lowerbound of the Ranges
	private static int higherBound = 10000; 		// UpperBound of the Ranges
	private static int rangesNumber = 10000; 		// Number of Ranges that will be generated
	private static int maxIteration = 100000; 		// Number of time "marcherLabel(X)" is called
	private static int maxNbThreads = 10; 			// Number of simultaneous threads running
	private static int nbOpeByThread = 500; 		// Number of time "marcherLabel(X)" is called by Thread
	private static List<BenchResult> benchResultList = new ArrayList<>(); // to manage time result
	

	// Write here which step(s) the main will do
	private static List<BenchMode> stepToDo = Arrays.asList( BenchMode.MONOTHREAD_MONOTREATMENT, BenchMode.MULTITHREAD_MONOTREATMENT,
			BenchMode.MULTITHREAD_MULTITREATMENT, BenchMode.MULTITHREAD_MULTITREATMENT_BENCH ); 

	// @formatter:on


	@SuppressWarnings("unused") // for "result" variables that are not used
	public static void main(String[] args)
    {
		// Manage command line options if given
		Options options = prepareOptions();
		manageOptions(options, args);


		// Auto generation of data for all tests
		List<Ranges> generatedRanges = RangesBuilder.generateRanges(rangesNumber, lowerBound, higherBound);
		List<Integer> items = RangesBuilder.generateItems(maxIteration, lowerBound, higherBound);
		RangesMatcher rm = new RangesMatcher(generatedRanges); // unique instance of "engine" for all tests
		Chrono chrono = new Chrono();

		/*
		 * Benchmarking in mono threading mode, one by one item
		 * -----------------------------------
		 * 
		 * Each iteration manage only one record
		 * 
		 */
		if (stepToDo.contains(BenchMode.MONOTHREAD_MONOTREATMENT)) {
			System.out.println("--== Benchmark Mono started ==--");
			printParameters(BenchMode.MONOTHREAD_MONOTREATMENT);


			chrono.pick();
			// Calling marchingLabel for each item
			List<List<String>> result = items.stream().map((Integer item) -> rm.marchingLabels(item))
					.collect(Collectors.toList());

			String timeInHHMMSS = chrono.compare();
			System.out.println("duration     : " + timeInHHMMSS);

			// Memorize result
			memorizeResult(BenchMode.MONOTHREAD_MONOTREATMENT, timeInHHMMSS, null, null);

			System.out.println("--== Benchmark ended ==--");
		}

		/*
		 * Benchmarking in multi-threading mode, one by one item
		 * ------------------------------------
		 * 
		 * Each record is managed in parallel thread but records are still treated one
		 * by one. A semaphore limit the number of current running threads.
		 * 
		 */
		if (stepToDo.contains(BenchMode.MULTITHREAD_MONOTREATMENT)) {
			System.out.println("\n--== Benchmark Multithread started ==--");
			printParameters(BenchMode.MULTITHREAD_MONOTREATMENT);

			Semaphore sema = new Semaphore(maxNbThreads);
			chrono.pick();
			try {
				List<Thread> threads = new ArrayList<>();
				for (int i = 0; i < maxIteration; i++) {
					sema.acquire();

					// For each iteration, we'll create a thread and launch it for a single call of
					// "marcherLabel()" (done in "run()" method of "MatcherLauncher")
					MatcherRunnable launcher = new MatcherRunnable();
					launcher.setItem(items.get(i));
					launcher.setSemaphore(sema);
					launcher.setRangesMatcher(rm);

					Thread t = new Thread(launcher);
					threads.add(t);
					t.start();
				}

				// waiting max 10min all thread's end before comparing Chrono
				threads.forEach(t -> {
					try {
						t.join(10 * 60 * 1000);
					} catch (InterruptedException e) {
					}
				}
				);
			} catch (InterruptedException e) {
				System.out.println("Error acquiring semaphore : " + e.getMessage());
			}

			String timeInHHMMSS = chrono.compare();
			System.out.println("duration     : " + timeInHHMMSS);

			// Memorize result
			memorizeResult(BenchMode.MULTITHREAD_MONOTREATMENT, timeInHHMMSS, maxNbThreads, null);

			System.out.println("--== Benchmark ended ==--");
		}

		/*
		 * Benchmarking in multi-threading mode and grouped records
		 * ------------------------------------
		 * 
		 * Each record is managed in parallel thread and records are treated by group of
		 * nbOpeByThread items. A semaphore limit the number of current running threads.
		 * 
		 */
		if (stepToDo.contains(BenchMode.MULTITHREAD_MULTITREATMENT)) {
			System.out.println("\n--== Benchmark Multithread MultiOperation started ==--");
			printParameters(BenchMode.MULTITHREAD_MULTITREATMENT);


			Semaphore sema = new Semaphore(maxNbThreads);

			// Preparing operations list that contains each "items" for each threads
			List<List<Integer>> operationsList = new ArrayList<>();
			List<Integer> operations = new ArrayList<>();
			int currentStackIndex = 0;
			for (int i = 0; i < maxIteration; i++) {
				if (currentStackIndex > nbOpeByThread) {
					currentStackIndex = 0;
					operationsList.add(operations);
					operations = new ArrayList<>();
				}

				operations.add(items.get(i));
				currentStackIndex++;
			}

			// Mapping Thread with its Runnable to retrieve Results
			Map<Thread, MatcherRunnable> threadRunnable = new HashMap<>();

			// starting bench
			chrono.pick();
			try {
				List<Thread> threads = new ArrayList<>();
				// for all lists we have prepared
				for (List<Integer> ope : operationsList) {
					sema.acquire();

					// preparing a Launcher with all operations for this thread
					MatcherRunnable launcher = new MatcherRunnable();
					launcher.setOperations(ope);
					launcher.setSemaphore(sema);
					launcher.setRangesMatcher(rm);

					Thread t = new Thread(launcher);
					threads.add(t);
					t.start();

					threadRunnable.put(t, launcher);
				}

				// waiting max 10min all thread's end before comparing Chrono
				threads.forEach(t -> {
					try {
						t.join(10 * 60 * 1000);
					} catch (InterruptedException e) {
					}
				});

				// fast checking results
				threads.forEach(t -> {
					MatcherRunnable r = threadRunnable.get(t);
					if (r.getResult().size() != nbOpeByThread) {
						System.out.println("Mistake on results, expected : " + nbOpeByThread + " founded :"
								+ r.getResult().size() + " results");
					}
				});

			} catch (InterruptedException e) {
				System.out.println("Error acquiring semaphore : " + e.getMessage());
			}
			String timeInHHMMSS = chrono.compare();
			System.out.println("duration     : " + timeInHHMMSS);

			// Memorize result
			memorizeResult(BenchMode.MULTITHREAD_MULTITREATMENT, timeInHHMMSS, maxNbThreads, nbOpeByThread);

			System.out.println("--== Benchmark ended ==--");
		}

		/*
		 * Benchmarking in multi-threading mode and grouped records to find which number
		 * of threads and which number of item is the more operate
		 * ------------------------------------
		 * 
		 * As in previous bench, a group of items is treated by a group of threads. The
		 * size of both is managed dynamically to find the more efficient combination
		 * 
		 */
		if (stepToDo.contains(BenchMode.MULTITHREAD_MULTITREATMENT_BENCH)) {
			System.out.println("\n--== Benchmark Multithread MultiOperation BenchMode config : ==--");
			printParameters(BenchMode.MONOTHREAD_MONOTREATMENT); // print just header

			// Preparing MaxNbThread Range
			List<Integer> maxNbThreadsRange = new ArrayList<>();
			// maxNbThreadsRange.add(1); // Too long...!
			maxNbThreadsRange.add(5);
			maxNbThreadsRange.add(10);
			maxNbThreadsRange.add(15);
			maxNbThreadsRange.add(20);
			maxNbThreadsRange.add(25);
			maxNbThreadsRange.add(30);

			// Preparing NbOpeByThread Range
			List<Integer> nbOpeByThreadRange = new ArrayList<>();
			// nbOpeByThreadRange.add(1); // Too long...!
			nbOpeByThreadRange.add(10);
			nbOpeByThreadRange.add(100);
			nbOpeByThreadRange.add(500);
			nbOpeByThreadRange.add(1000);
			nbOpeByThreadRange.add(5000);
			nbOpeByThreadRange.add(10000);
			nbOpeByThreadRange.add(50000);
			nbOpeByThreadRange.add(100000);
			nbOpeByThreadRange.add(500000);

			// Mapping Thread with its Runnable to retrieve Results
			Map<Thread, MatcherRunnable> threadRunnable = new HashMap<>();
			
			// Looping around number of threads and number of operations
			for (Integer mntr : maxNbThreadsRange) {
				for (Integer nobtr : nbOpeByThreadRange) {
					Semaphore sema = new Semaphore(mntr);

					// Preparing operations list
					List<List<Integer>> operationsList = new ArrayList<>();
					List<Integer> operations = new ArrayList<>();
					int currentStackIndex = 0;
					for (int i = 0; i < maxIteration; i++) {
						if (currentStackIndex >= nobtr) {
							currentStackIndex = 0;
							operationsList.add(operations);
							operations = new ArrayList<>();
						}

						operations.add(items.get(i));
						currentStackIndex++;
					}

					// starting bench with parameterized Number of Thread and Number of Items manage
					// by 1 thread
					System.out.println("\n--== Benchmark Starting ==--");
					printParameters(BenchMode.MULTITHREAD_MULTITREATMENT_BENCH, mntr, nobtr);

					chrono.pick();
					try {
						List<Thread> threads = new ArrayList<>();

						// For each Operations list
						for (List<Integer> ope : operationsList) {
							// Blocking if nbMawThread is reached
							sema.acquire();

							// preparing a Launcher with all operations for this thread
							MatcherRunnable launcher = new MatcherRunnable();
							launcher.setOperations(ope);
							launcher.setSemaphore(sema);
							launcher.setRangesMatcher(rm);

							Thread t = new Thread(launcher);
							threads.add(t);
							t.start();

							//Memorize Thread => Runnable
							threadRunnable.put(t, launcher);

						}

						// waiting max 10min all thread's end before comparing Chrono
						threads.forEach(t -> {
							try {
								t.join(10 * 60 * 1000);
							} catch (InterruptedException e) {
							}
						});

						// fast checking results
						threads.forEach(t -> {
							MatcherRunnable r = threadRunnable.get(t);
							if (r.getResult().size() != nobtr) {
								System.out.println("Mistake on results, expected : " + nobtr + " founded :"
										+ r.getResult().size() + " results");
							}
						});
					} catch (InterruptedException e) {
						System.out.println("Error acquiring semaphore : " + e.getMessage());
					}
					String timeInHHMMSS = chrono.compare();
					System.out.println("duration     : " + timeInHHMMSS);

					// Memorize result
					memorizeResult(BenchMode.MULTITHREAD_MULTITREATMENT_BENCH, timeInHHMMSS, mntr, nobtr);

					System.out.println("--== Benchmark ended ==--");
				}
			}
		}

		/*
		 * Analyzing results
		 */
		// sort by most efficient
		benchResultList.sort((BenchResult b1, BenchResult b2) -> b1.getDuration().compareTo(b2.getDuration()));

		// print sorted result
		System.out.println("\n---------------\nBests results :");
		benchResultList.forEach(b -> System.out.println(b));
	}

	public static void memorizeResult(BenchMode bm, String duration, Integer mntr, Integer nobtr) {
		BenchResult br = new BenchResult();
		br.setBenchMode(bm);
		br.setDuration(duration);
		br.setHigherBound(higherBound);
		br.setLowerBound(lowerBound);
		br.setMaxIteration(maxIteration);
		br.setRangeNumber(rangesNumber);
		br.setMaxNbThreads(mntr);
		br.setNbOpeByThread(nobtr);
		benchResultList.add(br);
	}

	public static void printParameters(BenchMode bm, Integer... params) {
		String print = "";
		switch (bm) {
		case MULTITHREAD_MONOTREATMENT:
			print += "MaxNbThreads  : " + maxNbThreads + "\n";
			print += "NbOpeByThread : 1";
		case MONOTHREAD_MONOTREATMENT:
			String temp = "LowerBound    : " + lowerBound + "\n";
			temp += "HigherBound   : " + higherBound + "\n";
			temp += "RangeNumber   : " + rangesNumber + "\n";
			temp += "MaxIteration  : " + maxIteration;

			print = temp + (print.isEmpty() ? "" : "\n" + print);
			break;
		case MULTITHREAD_MULTITREATMENT:
			print = "LowerBound    : " + lowerBound + "\n";
			print += "HigherBound   : " + higherBound + "\n";
			print += "RangeNumber   : " + rangesNumber + "\n";
			print += "MaxIteration  : " + maxIteration + "\n";

		case MULTITHREAD_MULTITREATMENT_BENCH:
			print += "MaxNbThreads  : " + (params.length > 0 ? params[0] : maxNbThreads) + "\n";
			print += "NbOpeByThread : " + (params.length > 1 ? params[1] : nbOpeByThread);
			break;
		default:
			break;
		}

		System.out.println(print);
	}

	public static Options prepareOptions() {
		Options options = new Options();

		options.addOption("h", "help", false, "Display this help message");
		options.addOption("b", "benchMode", true,
				"Define which step(s) will be done from 1 to 4 (ex: 1 or 23 or 124...) write 5 for all benches");
		options.addOption("l", "lowerBound", true, "Define the lower bound in Ranges");
		options.addOption("u", "upperBound", true, "Define the upper bound in Ranges");
		options.addOption("r", "rangesNumber", true, "Number of ranges automatically generated");
		options.addOption("i", "maxIteration", true, "Define the number of iteration");
		options.addOption("t", "maxNbThread", true, "Define the number of simultaneous thread");
		options.addOption("o", "nbOpeByThread", true, "Define the number of iterations by thread");
		options.addOption("p", "parameters", false, "Print default parameters values");
		options.addOption("j", "junit", false, "used by JUnit");

		return options;
	}

	public static void manageOptions(Options opt, String[] args) {

		HelpFormatter formatter = new HelpFormatter();

		try {
			CommandLineParser p = new DefaultParser();

			CommandLine cmd = p.parse(opt, args);

			if (cmd.hasOption("help")) {
				formatter.printHelp("ranges", opt);
				System.exit(0);
			}

			if (cmd.hasOption("lowerBound"))
				lowerBound = Integer.parseInt(cmd.getOptionValue("lowerBound"));

			if (cmd.hasOption("higherBound"))
				higherBound = Integer.parseInt(cmd.getOptionValue("higherBound"));

			if (cmd.hasOption("rangesNumber"))
				rangesNumber = Integer.parseInt(cmd.getOptionValue("rangesNumber"));

			if (cmd.hasOption("maxIteration"))
				maxIteration = Integer.parseInt(cmd.getOptionValue("maxIteration"));

			if (cmd.hasOption("maxNbThread"))
				maxNbThreads = Integer.parseInt(cmd.getOptionValue("maxNbThread"));

			if (cmd.hasOption("nbOpeByThread"))
				nbOpeByThread = Integer.parseInt(cmd.getOptionValue("nbOpeByThread"));

			if (cmd.hasOption("parameters")) {
				System.out.println("Step(s):");
				stepToDo.forEach(b -> System.out.println(b));
				printParameters(BenchMode.MULTITHREAD_MULTITREATMENT);
				System.exit(0);
			}

			String bm = null;
			if (cmd.hasOption("benchMode"))
				bm = cmd.getOptionValue("benchMode");

			if (bm != null && !bm.isEmpty()) {
				stepToDo = new ArrayList<>();

				for (int i = 0; i < bm.length(); i++) {
					switch (bm.charAt(i)) {
					case '1':
						stepToDo.add(BenchMode.MONOTHREAD_MONOTREATMENT);
						break;
					case '2':
						stepToDo.add(BenchMode.MULTITHREAD_MONOTREATMENT);
						break;
					case '3':
						stepToDo.add(BenchMode.MULTITHREAD_MULTITREATMENT);
						break;
					case '4':
						stepToDo.add(BenchMode.MULTITHREAD_MULTITREATMENT_BENCH);
						break;
					case '5':
						stepToDo.add(BenchMode.MONOTHREAD_MONOTREATMENT);
						stepToDo.add(BenchMode.MULTITHREAD_MONOTREATMENT);
						stepToDo.add(BenchMode.MULTITHREAD_MULTITREATMENT);
						stepToDo.add(BenchMode.MULTITHREAD_MULTITREATMENT_BENCH);
						break;

					default:
						break;
					}

				}
			}

			// if JUnit test, override all options
			if (cmd.hasOption("junit")) {
				System.out.println("JUnit mode");
				higherBound = 10;
				rangesNumber = 10;
				maxIteration = 10;
				nbOpeByThread = 5;
				stepToDo = Arrays.asList(BenchMode.MONOTHREAD_MONOTREATMENT, BenchMode.MULTITHREAD_MONOTREATMENT,
						BenchMode.MULTITHREAD_MULTITREATMENT, BenchMode.MULTITHREAD_MULTITREATMENT_BENCH);
			}

		} catch (ParseException e) {
			System.out.println("Error on parameters, see options :");

			formatter.printHelp("ranges", opt);

			System.exit(0);
		}

	}
}
