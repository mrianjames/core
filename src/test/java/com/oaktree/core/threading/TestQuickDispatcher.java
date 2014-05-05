package com.oaktree.core.threading;

import org.junit.Before;
import org.junit.Ignore;

@Ignore
public class TestQuickDispatcher extends TestDispatcher {

	@Before
	public void setup() {
         dispatcherType = DispatcherType.QUICK;
        super.setup();
    }

    @Override
    public void testDrainQueue() {
    }

    @Override
    public void testDispatcherMetrics() {

    }

    @Override
    public void testDispatchLatencyMT() {}

    @Override
    public void testMessagePriority() {}

}
