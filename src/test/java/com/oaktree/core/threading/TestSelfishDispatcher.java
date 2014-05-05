package com.oaktree.core.threading;

import org.junit.Before;
import org.junit.Ignore;

@Ignore
public class TestSelfishDispatcher extends TestDispatcher {

	@Before
	public void setup() {
         dispatcherType = DispatcherType.SELFISH;
        super.setup();
    }
}
