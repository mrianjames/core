package com.oaktree.core.pool;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class TestSimplePool {

	private static class ObjectFactory implements IObjectFactory<ByteBuffer> {
		@Override
		public ByteBuffer make() {
			ByteBuffer b = ByteBuffer.allocate(512);
			//System.err.println("Making "+System.identityHashCode(b) + " hash " + b.hashCode());
			return b;
		}
		
	}
	
	private static class IntegerFactory implements IObjectFactory<Integer> {
		public int calls = 0;
		@Override
		public Integer make() {
			calls++;
			return new Integer(0);
		}
		public int getCalls() {
			return calls;
		}
	}
	
	@Before
	public void setup(){}
	
	@After
	public void teardown(){}
	
	@Test
	public void testSimples() {
		IntegerFactory factory = new IntegerFactory();
		int capacity = 10;
		SimplePool<Integer> pool = new SimplePool<Integer>(capacity,factory);
		
		Assert.assertEquals(capacity, pool.getCapacity());
		Assert.assertEquals(capacity, factory.getCalls());
		Assert.assertNotNull(pool.get());
		Assert.assertTrue(pool.get().getClass().equals(Integer.class));
	}
	
	@Test
	public void testBounds() {
		IntegerFactory factory = new IntegerFactory();
		
		try {			
			SimplePool<Integer> pool = new SimplePool<Integer>(-1,factory);
			Assert.fail("Shoudl have barfed");
		} catch (Exception e) {
			//good
		}
		
		try {			
			SimplePool<Integer> pool = new SimplePool<Integer>(0,factory);
			Assert.fail("Shoudl have barfed");
		} catch (Exception e) {
			//good
		}
		
		try {			
			SimplePool<Integer> pool = new SimplePool<Integer>(1,null);
			Assert.fail("Shoudl have barfed");
		} catch (Exception e) {
			//good
		}
		
		try {			
			SimplePool<Integer> pool = new SimplePool<Integer>(10,factory);
			pool.setCapacity(8);
			Assert.fail("Shoudl have barfed");
		} catch (Exception e) {
			//good
		}
		
		try {			
			SimplePool<Integer> pool = new SimplePool<Integer>(10,factory);
			pool.setCapacity(0);
			Assert.fail("Shoudl have barfed");
		} catch (Exception e) {
			//good
		}

		try {			
			SimplePool<Integer> pool = new SimplePool<Integer>(10,factory);
			pool.setCapacity(-1);
			Assert.fail("Shoudl have barfed");
		} catch (Exception e) {
			//good
		}

		
	}
	
	@Test
	public void testExceptionWaitPolicy() {
		IntegerFactory factory = new IntegerFactory();
		
		SimplePool<Integer> pool = new SimplePool<Integer>(1,factory);
		pool.setWaitPolicy(new ExceptionWaitPolicy<Integer>());
		Integer a = pool.get(); //dont free.
		try {
			Integer b = pool.get();
			Assert.fail("Shoudl have barfed");
		} catch (Exception e) {
			//good.
		}
		pool.free(a);
		Assert.assertNotNull(pool.get());
		
	}
	
	@Test
	public void testNewObjectWaitPolicy() {
		IntegerFactory factory = new IntegerFactory();
		
		SimplePool<Integer> pool = new SimplePool<Integer>(1,factory);
		pool.setWaitPolicy(new NewObjectWaitPolicy<Integer>());
		
		Integer a = pool.get();
		Integer b = pool.get();
		Assert.assertNotSame(System.identityHashCode(a),System.identityHashCode(b));
		pool.free(a);
		Integer c = pool.get();
		Assert.assertEquals(System.identityHashCode(a),System.identityHashCode(c));
	}
	
	@Test
	public void testExpandObjectPolicy() {
		IntegerFactory factory = new IntegerFactory();
		ExpandPoolWaitPolicy<Integer> policy = new ExpandPoolWaitPolicy<Integer>(50, 4);
		policy.setDelegateWaitPolicy(new ExceptionWaitPolicy<Integer>());
		
		SimplePool<Integer> pool = new SimplePool<Integer>(1,factory);
		pool.setWaitPolicy(policy);
		
		Integer a = pool.get();
		Assert.assertEquals(1,pool.getCapacity());
		
		Integer b = pool.get();
		Assert.assertEquals(2,pool.getCapacity());
		Integer c = pool.get();
		Assert.assertEquals(3,pool.getCapacity());
		Integer d = pool.get();
		Assert.assertEquals(4,pool.getCapacity());
		Integer e = pool.get();
		Assert.assertEquals(6,pool.getCapacity());
		try {
			Integer f = pool.get();
			Integer g = pool.get();
			Assert.fail("Should have barfed");
		} catch (Exception t) {
			//good.
		}
		
	}
	
	@Test
	public void testFree() {
		IntegerFactory factory = new IntegerFactory();
		SimplePool<Integer> pool = new SimplePool<Integer>(1,factory);
		pool.setWaitPolicy(new ExceptionWaitPolicy<Integer>());
		Integer a = pool.get();
		try {
			Integer b = pool.get();
			Assert.fail("Should have barfed");
		} catch (Exception t) {
			//good.
		}
		pool.free(a);
		Integer b= pool.get(); //should be fine now.
	}
	
	@Test
	public void testGetIndexOfRecord() {
		IntegerFactory factory = new IntegerFactory();
		Integer stringa = new Integer(23);
		Integer stringb = new Integer(35);
		List<Integer> list = new ArrayList<Integer>();
		list.add(stringa);
		list.add(stringb);
		SimplePool<Integer> pool = new SimplePool<Integer>(1,factory);
		int index = pool.getIndexOfRecord(stringb,list);
		Assert.assertEquals(index,1);
	}
	
	@Test
	public void testMultithreadedIsSafe() throws InterruptedException {
		
		ObjectFactory factory = new ObjectFactory();
		int capacity = 10;
		final SimplePool<ByteBuffer> pool = new SimplePool<ByteBuffer>(capacity,factory);
		
		int THREADS = 5;
		final int MAX_WRITES_PER_THREAD = 1000;
		System.out.println("Threads: "+THREADS);
		Thread[] threads = new Thread[THREADS];
		final CountDownLatch latch = new CountDownLatch(THREADS);
		for (int x = 0; x < THREADS; x++) {
			final int threadid = x;
			Runnable r = new Runnable(){
				public void run() {
					
						int i = 0;
						while (MAX_WRITES_PER_THREAD == 0 || i < MAX_WRITES_PER_THREAD) {
							ByteBuffer buffer = pool.get();
							try {
								//System.err.println(System.nanoTime()+" " +Thread.currentThread().getName() +" got " + System.identityHashCode(buffer));
						        buffer.clear();
						        buffer.putLong(System.currentTimeMillis());
						        buffer.putShort((short)Thread.currentThread().getId());
						        buffer.putShort((short)800);
						        com.oaktree.core.utils.ByteUtils.putString(Thread.currentThread().getName(), buffer, 20);
						        com.oaktree.core.utils.ByteUtils.putString("Im a message", buffer, 128);
						        
							} catch (Exception e) {
								e.printStackTrace();
								System.err.println(System.nanoTime()+" " +Thread.currentThread().getName() +" Cannot process " + System.identityHashCode(buffer) + " " +e.getMessage());
							}
							pool.free(buffer);
							//System.err.println(System.nanoTime()+" " +Thread.currentThread().getName() +" Freed " + System.identityHashCode(buffer));
							i++;
						}
						System.out.println("Thread "+threadid + " is complete. I wrote " + i + " messages.");
						latch.countDown();
					
				}
			};
			threads[x] = new Thread(r);
			threads[x].setName("Thread"+x);
		}
			
		for (Thread t:threads) {
			t.start();
		}
		
		boolean success = latch.await(500000,TimeUnit.MILLISECONDS);
		Assert.assertTrue(success);
	}
}
