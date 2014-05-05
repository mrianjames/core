package com.oaktree.core.container.examples;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.container.IDynamicComponent;
import com.oaktree.core.container.IDynamicComponentFactory;
import com.oaktree.core.container.IMessage;


public class ExampleDynamicComponentFactory extends AbstractComponent implements IDynamicComponentFactory {

	private int counter = 0;
	@Override
	public IDynamicComponent[] resolve(IMessage message) {
		IDynamicComponent task =  new ExampleDynamicComponent();
		task.setName("MockDynamicComponent" + (counter++));
		task.setComponentType(ComponentType.TASK, null);
		return new IDynamicComponent[]{task};
	}


}
