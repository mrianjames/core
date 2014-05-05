
import com.oaktree.core.id.TestIdGenerator;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.oaktree.core.container.TestAbstractComponent;
import com.oaktree.core.container.TestComponentManager;
import com.oaktree.core.container.TestContainer;
import com.oaktree.core.container.TestFilteredMessageComponent;
import com.oaktree.core.latency.TestLatency;
import com.oaktree.core.search.TestReverseBinarySearch;
import com.oaktree.core.time.TestScheduler;
import com.oaktree.core.utils.TestCircularBuffer;
import com.oaktree.core.utils.TestMathUtils;
import com.oaktree.core.utils.TestUtilTimer;
import com.oaktree.core.threading.TestDispatcher;

/**
 * A JUnit 4 TestSuite for core package.
 * 
 * @author oaktree designs.
 * 
 */
@RunWith(Suite.class)
@SuiteClasses( { 
	TestAbstractComponent.class, 
	TestContainer.class,
	TestCircularBuffer.class, 
	TestUtilTimer.class,
	TestComponentManager.class, 
	TestScheduler.class,
	TestIdGenerator.class,
	TestFilteredMessageComponent.class,
	TestLatency.class,
	TestMathUtils.class,
	TestDispatcher.class,
	TestReverseBinarySearch.class
})
public class ManualSuiteCore {

}
