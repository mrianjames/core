package com.oaktree.core.container;

public interface IMsgCounterThresholdListener {

	/**
	 * Handle a warning limit breach
	 * @param counter
	 * @param count
	 */
	public void onMsgCounterWarningThreshold(MsgCounter counter, long count);

	/**
	 * Handle an error limit breach. 
	 * @param counter
	 * @param count
	 */
	public void onMsgCounterErrorThreshold(MsgCounter counter, long count);
	
	/**
	 * Handle an exception level breach. This is as bad as it gets
	 * and severe action will kick in (i.e. stop component)
	 * @param counter
	 * @param count
	 */
	public void onMsgCounterExceptionThreshold(MsgCounter counter, long count);
	
}
