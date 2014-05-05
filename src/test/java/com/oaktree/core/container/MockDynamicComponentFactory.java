package com.oaktree.core.container;

import com.oaktree.core.threading.policy.ComponentNameThreadingPolicy;
import com.oaktree.core.threading.policy.IThreadingPolicy;


public class MockDynamicComponentFactory extends AbstractComponent implements IDynamicComponentFactory {

	private static int counter = 0;
	private IThreadingPolicy policy;
	public void setThreadingPolicy(IThreadingPolicy policy) {
		this.policy = policy;
	}
	@Override
	public IDynamicComponent[] resolve(IMessage message) {
		IDynamicComponent task =  new MockDynamicComponent();
		task.setName("MockDynamicComponent" + counter);
		task.setThreadingPolicy(new ComponentNameThreadingPolicy(task));
		task.setComponentType(ComponentType.TASK, null);
		return new IDynamicComponent[]{task};
	}


}
