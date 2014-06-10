package com.oaktree.core.threading.dispatcher.Monitoring;

import com.oaktree.core.threading.dispatcher.IDispatcher;

/**
 * Listener for dispatch montioring events.
 *
 */
public interface IDispatcherListener {

	public void onDispatchStatistics(IDispatcher dispatcher,DispatchSnapshot snapshot);
}
