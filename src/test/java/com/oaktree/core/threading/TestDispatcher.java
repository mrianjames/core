package com.oaktree.core.threading;

import com.oaktree.core.logging.Log;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.dispatcher.SingleDispatcher.NullDispatcher;
import com.oaktree.core.threading.dispatcher.SingleDispatcher.SingleDispatcher;
import com.oaktree.core.threading.dispatcher.burner.BurnerDispatcher;
import com.oaktree.core.threading.dispatcher.mapped.MappedDispatcher;
import com.oaktree.core.threading.dispatcher.selfish.SelfishDispatcher;
import com.oaktree.core.threading.dispatcher.throughput.ThroughputDispatcher;
import com.oaktree.core.utils.CircularQueue;
import com.oaktree.core.utils.MathUtils;
import com.oaktree.core.utils.Text;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
public class TestDispatcher {

    private boolean canexpand = true;

    public TestDispatcher(){}

	private static final Logger logger = LoggerFactory.getLogger(TestDispatcher.class.getName());
	private int INSTRUMENTS = 10;
	public static class ShortRunningTask extends TestTask {
		public ShortRunningTask(int id, long s, List<Long> list,
				CountDownLatch latch,boolean recordStats) {
			super(id, s, list, latch,recordStats);
			// TODO Auto-generated constructor stub
		}
        public String toString() {
            return ""+id + "(" + s + ")";
        }

		@Override
		public void run() {
            //logger.info("Running task " + id + " latch: " + latch.getCount());
			if (recordStats) {
				this.dispatched = System.nanoTime();
			}
			if (this.updateList) {
				this.list.add(this.s);
			}
			latch.countDown();
			//System.out.println("Dispatched " + this.s);
		}
	}

	/**
	 * Varying repetitions of cpu intensive operations.
	 * @author Oak Tree Designs Ltd
	 *
	 */
	public static class LongProcessingTask extends TestTask {
		public LongProcessingTask(int id, long s, List<Long> list,
				CountDownLatch latch,boolean recordStats) {
			super(id, s, list, latch,recordStats);
		}

		@Override
		public void run() {
			/*
			 * a nice chunky calculation done a number of times to simulate processing on our dispatcher
			 * threads which stops other tasks from being dispatched.
			 */

			this.dispatched = System.nanoTime();
			int TASKS = 1000 * new Random(System.currentTimeMillis()).nextInt(100);
			for (int i = 0; i < TASKS; i++) {
				double[] src = new double[100];
				double[] dest = new double[100];
				System.arraycopy(src, 0, dest, 0, 100);
			}
			latch.countDown();
			if (this.updateList) {
				this.list.add(this.s);
			}
		}
	}
	
	private static class SleeperTask extends TestTask {
		public SleeperTask(int id, long s, List<Long> list,
				CountDownLatch latch,boolean recordStats) {
			super(id, s, list, latch,recordStats);
		}

		@Override
		public void run() {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public abstract static class TestTask implements Runnable {
		long s = 1;
		int id = 0;
		List<Long> list;
		CountDownLatch latch;
		boolean recordStats;
		public TestTask(int id, long s, List<Long> list,CountDownLatch latch, boolean recordStats) {
			this.s = s;
			this.latch = latch;
			this.list = list;
			this.recordStats = recordStats;
            this.id = id;
		}

		public void dispatch(long nanoTime) {
			this.start = nanoTime;
		}
		long start,dispatched = 0;
		boolean updateList = false;
		public long getDurationNano() {
			return dispatched - start;
		}
		public double getDurationMillis() {
			return this.getDurationNano() > 0 ? this.getDurationNano()/1000000 : 0;
		}
        public double getDurationMicros() {
			return this.getDurationNano() > 0 ? this.getDurationNano()/1000 : 0;
		}

	}

	String[] keys;
	List<TestTask> tasks;
	List<TestTask> longTasks;
	List<Long> results;
	int TASKS = 100000;
	private IDispatcher dispatcher;
	private int tpriority = Thread.NORM_PRIORITY;
	//private int tpriority = Thread.MAX_PRIORITY;
    public int threads = 2;
	private CircularQueue<String> cq;
	public CountDownLatch latch;
	private boolean doMessagePriority = false;
	//private static boolean selfish = false;
    public static enum DispatcherType { SELFISH,MAPPED,THROUGHPUT,SINGLE,NULL,BURNER,DISRUPTOR_MAPPED,QUICK};
    protected static DispatcherType dispatcherType = DispatcherType.THROUGHPUT;
	private static double perf;
	
	public String[] createLargeInstrumentSet() {
		return this.createLargeInstrumentSet(INSTRUMENTS);
	}
	public String[] createLargeInstrumentSet(int count) {
		String[] instruments = new String[count];
		for (int i = 0; i < INSTRUMENTS; i++) {
			instruments[i] = "A" + i;
		}
		return instruments;
	}

	@Before
	public void setup() {
		this.keys = //new String[]{"VOD.L","BARC.L","PSON.L","GSK.L"};
			this.createLargeInstrumentSet();
		this.cq = new CircularQueue<String>();
		for (String k:this.keys) {
			this.cq.add(k);
		}
		//this.cq.add("VOD.L");
		switch (dispatcherType) {

            case MAPPED:
			    this.setupMappedDispatcher();
                break;
			case SELFISH:
                this.setupSelfishDispatcher();
                break;
            case THROUGHPUT:
                logger.info("Setting up throughput dispatcher");
                this.setupThroughputDispatcher();
                break;
            case SINGLE:
                this.setupSingleDispatcher();
                break;
            case NULL:
                this.setupNullDispatcher();
                break;
            case BURNER:
                this.setupBurnerDispatcher();
                break;
//            case DISRUPTOR_MAPPED:
//                this.setupDisruptorMappedDispatcher();
//                break;
//            case QUICK:
//                this.setupQuickDispatcher();
        }
		logger.info("Dispatcher: " + this.dispatcher.getName());
		//this.setupGDispatcher();
		this.tasks = new ArrayList<TestTask>();
		this.longTasks = new ArrayList<TestTask>();
		this.results = new ArrayList<Long>();
		this.latch = new CountDownLatch(TASKS);
		for (int i = 0; i < TASKS; i++) {
			TestTask task = new ShortRunningTask(i,(long)i,results,latch,true);
			tasks.add(task);
		}
		for (int i = 0; i < TASKS; i++) {
			TestTask task = new LongProcessingTask(i,(long)i,results,latch,true);
			longTasks.add(task);
		}
		logger.info("Test tasks created...commencing test run");
	}

//    private void setupQuickDispatcher() {
//        QuickDispatcher d = new QuickDispatcher("test",threads);
//        d.start();
//        this.dispatcher = d;
//    }
//
//    private void setupDisruptorMappedDispatcher() {
//        DisruptorD dispatcher = new DisruptorMappedDispatcher("test",threads);
//        dispatcher.setCanExpand(canexpand);
//        dispatcher.setKeys(keys);
//        dispatcher.start();
//        this.dispatcher = dispatcher;
//    }

    private void setupNullDispatcher() {
        dispatcher = new NullDispatcher();
        dispatcher.setName("NullDispatcher");
        dispatcher.start();
    }
    private void setupSingleDispatcher() {
        dispatcher = new SingleDispatcher();
        dispatcher.setName("SingleDispatcher");
        dispatcher.start();
    }

    private void setupThroughputDispatcher() {
		this.dispatcher = new ThroughputDispatcher("verpasian",keys,this.threads);
		ThreadFactory factory = new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("ThroughputDispatcher");
				t.setDaemon(true);
				t.setPriority(tpriority);
				return t;
			}

		};
		((ThroughputDispatcher)this.dispatcher).setThreadFactory(factory);
		((ThroughputDispatcher)this.dispatcher).setCanExpand(canexpand);
        ((ThroughputDispatcher)this.dispatcher).setDominiationCount(10);
		dispatcher.setName("ThroughputDispatcher");
		dispatcher.start();
	}

	private void setupMappedDispatcher() {
		this.dispatcher = new MappedDispatcher("verpasian",this.threads,keys);
		dispatcher.setName("MappedDispatcher");
        ((MappedDispatcher)this.dispatcher).setCanExpand(canexpand);
		dispatcher.start();
	}
	
	private void setupSelfishDispatcher() {
		this.dispatcher = new com.oaktree.core.threading.dispatcher.selfish.SelfishDispatcher("IJ",this.threads);
		this.dispatcher.setKeys(keys);
		dispatcher.setName("SelfishDispatcher");
        ((SelfishDispatcher)this.dispatcher).setCanExpand(canexpand);
		dispatcher.start();
	}

    private void setupBurnerDispatcher() {
		this.dispatcher = new BurnerDispatcher("IJ");
		dispatcher.start();
	}

//	private void setupSimpleDispatcher() {
//		this.dispatcher = new com.oaktree.core.threading.old.SimpleDispatcher("IJ",this.threads);
//		this.dispatcher.setKeys(keys);
//		dispatcher.setName("Simple");
//		((SimpleDispatcher)this.dispatcher).setCanExpand(false);
//		if (this.doMessagePriority) {
//			((SimpleDispatcher)this.dispatcher).setAllowPrioritisation(true);
//		}
//		((SimpleDispatcher)this.dispatcher).setThreadPriority(this.tpriority);
//		this.dispatcher.setThreads(this.threads);
//		dispatcher.start();
//	}

	@After
	public void tearDown() {
		dispatcher.stop();
	}

	static DecimalFormat format = new DecimalFormat("0.0000");

	@Test
	public void testDispatcherMetrics() {
		if ((this.dispatcher instanceof SelfishDispatcher)) {
			return;
		}
		this.results.clear();
		this.tasks.clear();
		int runs = 10;
		this.latch = new CountDownLatch(runs);
		for (int i = 0; i < runs; i++) {
			TestTask task = new LongProcessingTask(0,(long)i,results,latch,true);
			task.updateList = true;
			tasks.add(task);
		}
		Assert.assertEquals(this.dispatcher.getQueuedTaskCount("VOD.L"),0,EPSILON);
		Assert.assertEquals(this.dispatcher.getQueuedTaskCount("MONKEY"),0,EPSILON);

		for (int i = 0; i < runs; i++) {
			TestTask task = tasks.get(i);
			task.latch = this.latch;
			task.dispatch(System.nanoTime());
			dispatcher.dispatch("VOD.L",task,5);
		}
        Assert.assertTrue(this.dispatcher.getQueuedTaskCount("VOD.L")> 0);

		try {
			this.latch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		//sleep is needed as though latch has been unlocked the execution count
		//may not have been updated (its done after task done, after latch unlock
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		//exec count is approximate by design (for performance) but all tasks should have
		//update the counter by now.
		Assert.assertEquals(this.dispatcher.getExecutedTaskCount("VOD.L"),runs,EPSILON);
		Assert.assertEquals(this.dispatcher.getExecutedTaskCount("MONKEY"),0,EPSILON);
	}

	@Test(timeout=4000000)
	public void testPerformance() {
		try {
		ListIterator<String> it = this.cq.listIterator();
		long s = System.nanoTime();
		boolean x = false;
            //TASKS = 100;
            //latch = new CountDownLatch(TASKS);
		for (int i = 0; i < TASKS; i++) {
			TestTask task = tasks.get(i);
            //task.latch = latch;
			//task.dispatch(System.nanoTime());
			dispatcher.dispatch(it.next(),task,x ? 5 : 6);
			x = !x;
		}
		try {
			this.latch.await();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long e = System.nanoTime();

//		DescriptiveStatistics stats = new DescriptiveStatistics();
//		for (TestTask task:tasks) {
//			stats.addValue(task.getDurationNano());
//		}

		double d = ((double)(e-s))/1000000d;
		double avgdur = d/(double)TASKS;
		double permilli = 1d/(avgdur);
		perf = permilli;
		double persec = 1000*permilli;
		logger.info(TASKS  + " tasks completed in " + d + " ms, avging " + format.format(avgdur) + " ms, or " + (int)permilli + " per milli, " + (int)persec + " per sec" /*, med: " + format.format(stats.getPercentile(0.5)/1000000) + " 90%:" + format.format(stats.getPercentile(0.9)/1000000)*/);

		} catch(Throwable t) {
			t.printStackTrace();
			Log.exception(logger,t);
		}

		System.gc();

	}


	@Test(timeout=4000000)
	public void testEveryLongRunningTaskExecuted() {
		TASKS = 200;
		this.latch = new CountDownLatch(TASKS);
		try {
			ListIterator<String> it = this.cq.listIterator();
			long s = System.nanoTime();
			for (int i = 0; i < TASKS; i++) {
				TestTask task = longTasks.get(i);
				task.latch = this.latch;
				task.dispatch(System.nanoTime());
				dispatcher.dispatch(it.next(),task,5);
			}
			try {
				this.latch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			long e = System.nanoTime();

			DescriptiveStatistics stats = new DescriptiveStatistics();
			for (TestTask task:longTasks) {
				stats.addValue(task.getDurationMillis());
			}
			DecimalFormat format = new DecimalFormat("0.00000000000000");
			double d = ((double)(e-s))/1000000d;
			double avgdur = d/(double)TASKS;
			double permilli = 1d/(avgdur);
			double persec = 1000*permilli;
			System.out.println(TASKS  + " long tasks completed in " + d + " ms, avging " + format.format(avgdur) + " ms, or " + (int)permilli + " per milli, " + (int)persec + " per sec");

		} catch(Throwable t) {
			t.printStackTrace();
		}

	}

    public int TESTS = 1000000-1;

	@Test(timeout=4000000)
	public void testMessagePriority() {
        int PTESTS = 300;
		this.latch = new CountDownLatch(PTESTS);
		TestTask task2 = longTasks.get(PTESTS);
		task2.list = this.results;
		task2.latch = this.latch;
		task2.updateList = true;
        //0-99998 records
		for (int i = 0; i < PTESTS-1; i++) {
			TestTask task = this.longTasks.get(i); //forces queues to build that we can jump
			task.list = this.results;
			task.updateList = true;
			task.latch = this.latch;
			task.dispatch(System.nanoTime());
			dispatcher.dispatch("VOD.L",task, IDispatcher.NORMAL_PRIORITY);
		}
        //100000 task
		dispatcher.dispatch("VOD.L",task2,IDispatcher.HIGH_PRIORITY);
		try {
			this.latch.await();
		} catch (InterruptedException e1) {
		}
        logger.info("Result items: " + results.size());
		Assert.assertEquals(results.size(),PTESTS);
		long lastid = this.results.get(this.results.size()-1);
		logger.info("Last id: " + lastid);
		int idx = this.results.indexOf(Long.valueOf(task2.s));
		logger.info("Task got priority shunted to position " + idx);
		if (!(this.dispatcher instanceof SelfishDispatcher)) {
			Assert.assertTrue(idx<results.size()-1);
		}
	}

	@Test(timeout=4000000)
	public void testMessagePrioritySamePrioritySequential() {
		/*
		 * TEST is to check that equal priority messages are always treated sequentially
		 * when doMessagePriority is on.
		 */
		if ((this.dispatcher instanceof SelfishDispatcher)) {
			return;
		}
		int TESTS = 1000;
		this.latch = new CountDownLatch(TESTS);
		TestTask task2 = tasks.get(TESTS);
		task2.list = this.results;
		task2.latch = this.latch;
		task2.updateList = true;
		for (int i = 0; i < TESTS-1; i++) {
			TestTask task = tasks.get(i);
			task.list = this.results;
			task.updateList = true;
			task.latch = this.latch;
			task.dispatch(System.nanoTime());
			dispatcher.dispatch("VOD.L",task,IDispatcher.NORMAL_PRIORITY);
		}
		dispatcher.dispatch("VOD.L",task2,IDispatcher.NORMAL_PRIORITY);
		try {
			this.latch.await();
		} catch (InterruptedException e1) {
		}

		Assert.assertEquals(results.size(),TESTS);
		long lastid = this.results.get(this.results.size()-1);
		logger.info("Last id: " + lastid);
		int idx = this.results.indexOf(Long.valueOf(task2.s));
		logger.info("Task got priority shunted to position " + idx);
		if (!(this.dispatcher instanceof SelfishDispatcher)) {
			Assert.assertFalse(idx<results.size()-1);
		}
	}

	volatile boolean fail = false;

	@Test
	public void testInitial() throws InterruptedException {
//		final ThroughputDispatcher d = new ThroughputDispatcher();
//		d.setThreads(4);
//		d.setName("ThroughputDispatcher");
//		d.start();
		final AtomicInteger testVar = new AtomicInteger();

		//d.dispatch("ABC123", new TestEvent(testVar));

		//Thread.sleep(1000);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < 1000; i++) {
					dispatcher.dispatch("ABC123", new TestEvent(testVar));
				}
			}

		};

		Thread[] threads = new Thread[3];
		for(int i=0;i<threads.length;i++){
			threads[i] = new Thread(runnable);
		}
		for(int i=0;i<threads.length;i++){
			threads[i].start();
		}

		Thread.sleep(3000);
		Assert.assertFalse(fail);
	}

	public class TestEvent implements Runnable {

		AtomicInteger testVar;

		public TestEvent(AtomicInteger testVar) {
			super();
			this.testVar = testVar;
		}

		@Override
		public void run() {
			//logger.info("Starting");
			int initialValue = testVar.getAndIncrement();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			int finalValue = testVar.decrementAndGet();
			//logger.info("Finishing:" + initialValue + " " + finalValue);
			if (finalValue != 0) {
				TestDispatcher.this.fail = true;
			}
		}

	}

	@Test(timeout=400000)
	public void testEveryTaskExecuted() {

		ListIterator<String> it = this.cq.listIterator();
		
		boolean x = false;
		for (int i = 0; i < TASKS; i++) {
			TestTask task = tasks.get(i);
			task.dispatch(System.nanoTime());
			dispatcher.dispatch(it.next(),task,x ? 5 : 6);
			x = !x;
		}
		try {
			this.latch.await(100,TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Assert.assertEquals(0,latch.getCount(),EPSILON);
	}

	@Test(timeout=400000)
	public void testSequentialExecution() {
		this.results.clear();
		this.tasks.clear();
		int X = TASKS/10;
		this.latch = new CountDownLatch(X);
		for (int i = 0; i < X; i++) {
			TestTask task = new ShortRunningTask(0,(long)i,results,latch,false);
			task.updateList = true;
			tasks.add(task);
		}

		for (int i = 0; i < X; i++) {
			TestTask task = tasks.get(i);
			dispatcher.dispatch("VOD.L",task,5);
		}
		System.out.println("Dispatched All.");
		try {
			this.latch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Assert.assertEquals(latch.getCount(),0,EPSILON);
		Assert.assertEquals(this.results.size(),X,EPSILON);
		for (int i = 0; i < (X)-1; i++) {
            Long a = this.results.get(i);
            Long b = this.results.get(i+1);
            if (b == null) {
                logger.warn("Odd....b null: "+  i);
            }
			if (a<b == false) {
				logger.info("Out of sequence: " + this.results.get(i) + ", " + this.results.get(i+1));
			}
			Assert.assertTrue(this.results.get(i)<this.results.get(i+1));
		}
	}
	
	@Test
    @Ignore
	public void testDrainQueue() {
        logger.info("Running drain queue in class " + this.getClass().getName());
		this.tasks.clear();

        for (int i = 0; i < 1000; i++) {
			TestTask task = new SleeperTask(0,(long)i,results,latch,false);
			task.updateList = false;
			tasks.add(task);
		}
		
		for (int i = 0; i < 1000; i++) {
			TestTask task = tasks.get(i);
			String symbol = "A";
			dispatcher.dispatch(symbol,task,5);
		}
		
		List<Runnable> drainage = new ArrayList<Runnable>();
		dispatcher.drainQueue(drainage,"A");
		logger.info("Drainqueue size:" + drainage.size());
        Assert.assertTrue(drainage.size() > 0);

        try {Thread.sleep(2000); } catch (Exception e) {}

	}

	@Test(timeout=400000)
	public void testSequentialExecutionPerName() {
		this.tasks.clear();
		for (int i = 0; i < TASKS; i++) {
			TestTask task = new ShortRunningTask(0,(long)i,results,latch,false);
			task.updateList = true;
			tasks.add(task);
		}
		Iterator<String> it = this.cq.iterator();
		Map<String,List<Long>> m = new HashMap<String,List<Long>>();
		for (int i = 0; i < TASKS; i++) {
			TestTask task = tasks.get(i);
			String symbol = it.next();
			task.list = m.get(symbol);
			if (task.list == null) {
				task.list = new ArrayList<Long>();
				m.put(symbol, task.list);
			}
			//task.dispatch(System.nanoTime());
			dispatcher.dispatch(symbol,task,5);
		}

		try {
			this.latch.await(10,TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Assert.assertEquals(latch.getCount(),0,EPSILON);
		for (List<Long> l: m.values()) {
			for (int i = 0; i < l.size()-1; i++) {
				Assert.assertTrue(l.get(i)<l.get(i+1));
			}
		}
	}


	private double EPSILON = 0.00000000001;

	public static void main(String[] args) {
		if (args.length < 6) {
			logger.error("threads,keys,tests,loops,type,tpriority,test_type");
			System.err.println("threads, keys, tests, type,loops,tpriority,test_type");
			//System.exit(-1);
                        //default parameters...
                        args = new String[]{"8","32","10000","25","false","5","latency"};
		}

		int threads = Integer.valueOf(args[0]);
		int keys = Integer.valueOf(args[1]);
		int tests = Integer.valueOf(args[2]);
		int loops = Integer.valueOf(args[3]);
		int priority = Integer.valueOf(args[5]);
        boolean throughput = args[6].equals("throughput");
		logger.info("Threads: " + threads);
		logger.info("Keys: " + keys);
		logger.info("Tests: " + tests);
		logger.info("Loops: " + loops);
		logger.info("TPriority: " + priority);
        logger.info("Arch: " + System.getProperty("sun.arch.data.models"));
		TestDispatcher dispatcher = new TestDispatcher();
		dispatcher.threads = threads;
		dispatcher.INSTRUMENTS = keys;
        dispatcher.canexpand = false; //for performance.
		dispatcher.TASKS = tests;
		dispatcher.tpriority = priority;
		TestDispatcher.dispatcherType = DispatcherType.valueOf(args[4]);
		logger.info("DispatcherType: " + TestDispatcher.dispatcherType);
		double total = 0;
		dispatcher.setup();
		for (int i = 0; i < loops; i++) {
			if (throughput) {
                dispatcher.testPerformance();
            } else {
                dispatcher.testDispatchLatency();
            }
			if (i > 0) {
				total += TestDispatcher.perf;
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dispatcher.tearDown();
        if (throughput) {
            logger.info("Avg perf: " + TestDispatcher.format.format((total/(loops-1)))  + " per milli.");
        } else {
            logger.info(lats.getValues().length+ " items. Median: "+ Text.to2Dp(lats.getPercentile(50)) + "us 10%: " + Text.to2Dp(lats.getPercentile(10)) + "us 90%:"  + Text.to2Dp(lats.getPercentile(90)) + "us 95%: " + Text.to2Dp(lats.getPercentile(95)) + "us 99%: " + Text.to2Dp(lats.getPercentile(99.0)) + "us. Min: " + Text.to2Dp(lats.getMin()) + "us. StdDev: "+ Text.to2Dp(lats.getStandardDeviation()));
        }

	}

    static DescriptiveStatistics lats = new DescriptiveStatistics();
	
	@Test(timeout=4000000)
	public void testDispatchLatency() {
		logger.info("STARTING");
		this.tasks.clear();
		int X = 100000;
		this.latch = new CountDownLatch(X);
		for (int i = 0; i < X; i++) {
			TestTask task = new ShortRunningTask(0,(long)i,results,latch,true);
			task.updateList = false;
			tasks.add(task);
		}
		Iterator<String> it = this.cq.iterator();
		for (int i = 0; i < X; i++) {
			TestTask task = tasks.get(i);
			String symbol = it.next();			
			task.dispatch(System.nanoTime());
			dispatcher.dispatch(symbol,task,5);
//            if (i % 20 == 0) {
//                try { Thread.sleep(1); } catch (Exception e) {}
//            }
		}
		logger.info("BLASTED - WAITING");
		try {
			this.latch.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		logger.info("ALL PROCESSED");

		Assert.assertEquals(latch.getCount(),0,EPSILON);
		DescriptiveStatistics s = new DescriptiveStatistics();
		for (TestTask task:tasks) {
            double nanos = task.getDurationNano();
            if (!MathUtils.isGreaterThanZero(nanos)) {
                logger.warn("Task " + task.id + " has 0 or negative time: " + nanos );
            }
			s.addValue(nanos/1000);
		}
		logger.info(X+ " items. Median: "+ Text.to2Dp(s.getPercentile(50)) + "us 10%: " + Text.to2Dp(s.getPercentile(10)) + "us 90%:"  + Text.to2Dp(s.getPercentile(90)) + "us 95%: " + Text.to2Dp(s.getPercentile(95)) + "us 99%: " + Text.to2Dp(s.getPercentile(99.0)) + "us. Min: " + Text.to2Dp(s.getMin()) + "us. StdDev: "+ Text.to2Dp(s.getStandardDeviation()));
        perf = s.getPercentile(50);
        for (double v:s.getValues()) {
            lats.addValue(v);
        }
	}

    @Test(timeout=4000000)
    public void testDispatchLatencyMT() {
        logger.info("STARTING");
        this.tasks.clear();
        final int X = 10000;
        final int threads = 2; //number fast feeds coming in providing dispatchable events on a key. 3 seems to be saturation point.
        Thread[] tds = new Thread[threads];
        this.latch = new CountDownLatch(X*threads);
        final List<TestTask> alltasks = new ArrayList<TestTask>();
        for (int t = 0;t < threads; t++) {
            final int u = t;
            tds[t] = new Thread(new Runnable(){
                public void run() {
                    List<TestTask> tasks = new ArrayList<TestTask>();
                    int y = u * X;
                    for (int i = 0; i < X; i++) {
                        TestTask task = new ShortRunningTask(0,(long)i+y,results,latch,true);
                        task.updateList = false;
                        tasks.add(task);
                    }
                    synchronized (alltasks) {
                        alltasks.addAll(tasks);
                    }
                    Iterator<String> it = cq.iterator();
                    logger.info("BLASTING on " + u);
                    for (int i = 0; i < X; i++) {
                        TestTask task = tasks.get(i);
                        String symbol = it.next();
                        task.dispatch(System.nanoTime());
                        dispatcher.dispatch(symbol,task,5);
//                        try {
//                            Thread.sleep(1);
//                        } catch (InterruptedException e) {
//                        }
                    }
                }
            });
        }
        for (Thread t:tds) {
            t.start();
        }
        logger.info("BLASTED - WAITING");
        try {
            this.latch.await();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        logger.info("ALL PROCESSED");

        Assert.assertEquals(latch.getCount(),0,EPSILON);
        DescriptiveStatistics s = new DescriptiveStatistics();
        for (TestTask task:alltasks) {
            double nanos = task.getDurationNano();
            if (!MathUtils.isGreaterThanZero(nanos)) {
                logger.warn("Task " + task.id + " has 0 or negative time: " + nanos );
            }
            s.addValue(nanos/1000);
        }
        logger.info((X*threads)+ " items. Median: "+ Text.to2Dp(s.getPercentile(50)) + "us 10%: " + Text.to2Dp(s.getPercentile(10)) + "us 90%:"  + Text.to2Dp(s.getPercentile(90)) + "us 95%: " + Text.to2Dp(s.getPercentile(95)) + "us 99%: " + Text.to2Dp(s.getPercentile(99.0)) + "us. Min: " + Text.to2Dp(s.getMin()) + "us.");
        perf = s.getPercentile(50);
    }

}
