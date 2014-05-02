package com.oaktree.core.threading;

import org.junit.Before;

public class TestMappedDispatcher extends TestDispatcher {

	@Before
	public void setup() {
         dispatcherType = DispatcherType.MAPPED;
        super.setup();
    }

//    @Test
//    public void testMessagePriority() {
//        threads = 1;
//        TESTS = 100000-1;
//        TASKS = 1000000-1;
//        tasks.clear();
//        for (int i = 0; i < TASKS; i++) {
//            //TestTask task = new LongProcessingTask(i,(long)i,results,latch,true);
//            TestTask task = new PrintingTask(i,(long)i,results,latch,true);
//            tasks.add(task);
//        }
//        super.testMessagePriority();
//    }
//
//    private class PrintingTask extends TestTask {
//        public PrintingTask(int id, long s, List<Long> list, CountDownLatch latch, boolean recordStats) {
//            super(id, s, list, latch, recordStats);
//        }
//
//        @Override
//        public void run() {
//            System.out.println("Task: " + id);
//        }
//    }

}
