package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;
import com.oaktree.core.threading.dispatcher.IDispatcher;

/**
 * Sequence that dispatches what came in to the next step using the dispatcher.
 * 
 * @author ij
 *
 * @param <I>
 * @param <O>
 */
public class DispatchSequence<I,O> extends DataSequence<I,O> implements	IDataSequence<I, O> {

	private IDispatcher dispatcher;

	public DispatchSequence(String name, IDispatcher dispatcher) {
		super(name);
		this.dispatcher = dispatcher;
	}
	
    @Override
	public void onData(I data, IComponent from,final long receivedTime) {
		//distribute the result via the dispatcher to each receiver we have using its name as the dispatcher key.
		final O updated = process(data,from,receivedTime);
		for (final IDataReceiver<O> sink: getReceivers()) {
			dispatcher.dispatch(sink.getName(), new Runnable() {
				public void run() {
					sink.onData(updated, DispatchSequence.this, receivedTime);		
				}
			});			
		}
	}

    @Override
    public String toString() {
    	return "DispatcherSequence["+getName()+"]";
    }
}
