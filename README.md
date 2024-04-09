# Ranges
See ranges.pdf for full exercise instructions.

# My choices
I've choose to write a Java CLI application beacause this exercise doesn't need any interaction when it's running.
No Spring or other, just a Maven for easy dependency management and easy build.
As a CLI App, it can be driven on launch by command-line parameters.
Hit -h to see them ;)

I pushed this exercise as a demonstration of the efficiency of multi-threading
4 types of bench mark is proposed,
Each is timed and is compared as "Who is the faster?"

1st one is a simple lambda loop, which calls the function for each item.
2nd is a multi-thread launch but each thread only runs 1 call for 1 item.
3rd is a multi-thread launch with a bunch of items to have multiple calls in 1 thread.
4th is a large multi-threading benchmark, executing several times the same test but with a different number of maximum thread and a different number of items.
In other words :
App has to do 100 000 calls to the function
- Number of simultaneous thread is contains between 5 and 30 (step of 5 : 5, 10, 15, 20, 25, 30).
- Each thread will treat between 5 and 50 000 items (gradual step : 5, 10, 100, 1 000, 5 000, 10 000, 50 000, ...)
  
As this we can have a large comparator on how efficient is the multi-threading... and its limits.
 
# Conclusion
We have a huge difference between no-threading and multi-threading.
But adding indefinitely thread is not a solution; it has a heavy cost (in time) to launch.
(The 2nd solution is much slower than the 1st.)
The 4th shows that having too many threads is not the best solution.
Thread concurrence may cause processor scheduling to switch too frequently and slow down global efficiency.
The best number of threads for my computer seems to be near 10.

Concerning Items,
For sure, if I start a thread to do only 1 call of the function... it takes too much time.
So we can see in this bench mark that the more threads have items to compute "the faster it is"...
Yes with "quote".
Because, imagine you have 10 000 items, 10 threads, but "each" thread can manage 10 000 items.
(as it is developed here), All 10 000 items will go to the 1st thread and others won't have nothing to proced...
In this case, it's like we're on mono-thread... so longer than a real multi-threading.
The optimization with items consists of seeing:
- how many threads I can launch
- how many items still have to proced
  
And with this, define the good balance of the number of item by lasting thread.
The most optimal balance is to ensure that all lasting threads ended at the same time!
