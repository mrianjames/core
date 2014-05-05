package com.oaktree.core.threading.dispatcher.mapped;

import com.oaktree.core.threading.dispatcher.IDispatcher;

/**
 * A wrapper round a task we need to run. binds a key, priority and task together.
 */
public class DispatchTask implements Runnable {
		private int priority = IDispatcher.NORMAL_PRIORITY;
		private Runnable task;
		private String key;
		public DispatchTask(String key,Runnable task) {
			this.task = task;
			this.key = key;
		}
		public DispatchTask(String key, Runnable task, int priority) {
			this(key,task);
			this.priority = priority;
		}
		public void run() {
			this.task.run();
		}
		public String getKey() {
			return this.key;
		}
		public int getPriority() {
			return this.priority;
		}
	}
