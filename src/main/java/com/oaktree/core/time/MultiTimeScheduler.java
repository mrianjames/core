package com.oaktree.core.time;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.utils.Text;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * The multi time scheduler is a version of a scheduler of tasks where time can be speeded up, slowed down and
 * events can be played one at a time. There are two modes of operation:
 * 1) Execution Mode. Tasks are scheduled at real or hypertime and executed when specified in that time models.
 * 2) Click Mode. Tasks are clicked through one at a time.
 * 
 * You can also pause execution of tasks and resume (optionally playing the messages you missed whilst
 * paused) though this feature is only when in execution mode.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class MultiTimeScheduler extends AbstractComponent implements ITimeScheduler {

    private ITime time = new JavaTime();
    private IDispatcher dispatcher;

    /**
     * Time unit all scheduling occurs in.
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    
    public MultiTimeScheduler() {}
    public MultiTimeScheduler(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    public MultiTimeScheduler(IDispatcher dispatcher, TimeUnit timeUnit) {
        this(dispatcher);
        this.timeUnit = timeUnit;
    }

    public void setTime(ITime time) {
        this.time = time;
    }

    private double hypertime = 1;
	private long start;
	private long realstart;
	private ScheduledThreadPoolExecutor timer;
	
	/**
	 * The task we actually supply to the underlying scheduler.
	 * @author Oak Tree Designs Ltd
	 *
	 */
	private static class ScheduledTask implements Runnable {
		private Runnable runable;
		private boolean run;
		private boolean completed;
		private MultiTimeScheduler scheduler;
		private ScheduledFuture<?> future;
		private double hypertime;
        private String name;

        ScheduledTask(String nameable,Runnable r, MultiTimeScheduler s, double hypertime) {
			this.runable = r;
            this.name = nameable;
			this.scheduler = s;
			this.hypertime = hypertime;
		}
		public void run() {
			synchronized (scheduler) {
				if (!scheduler.clickMode && !scheduler.isPaused()) {
					this.doRun();
				} else {
					scheduler.addClickTask(this);
				}
			}
		}
		public double getHypertime() {
			return this.hypertime;
		}
		void doRun() {
			this.run = true;
            if (scheduler.dispatcher!= null && name != null) {
                scheduler.dispatcher.dispatch(name,runable);
            } else {
			    this.runable.run();
            }
			this.completed = true;
		}
		public void setFuture(ScheduledFuture<?> v) {
			this.future = v;
		}
		public ScheduledFuture<?> getFuture() {
			return this.future;
		}
		public void cancel() {
			this.future.cancel(false);
		}
		public Runnable getRunable() {
			return runable;
		}
		public boolean isRun() {
			return run;
		}
		public boolean isCompleted() {
			return completed;
		}
	}
	
	boolean clickMode = false;
	
	/**
	 * Tasks that are not for immediate execution are buffered up in a list for click
	 * or later play back. Used in click mode and when in execution mode and paused.
	 */
	private List<ScheduledTask> executedTaskBuffer = new CopyOnWriteArrayList<ScheduledTask>();
	
	@Override
	public void cancel(Future<?> appointment) {
		appointment.cancel(false);
	}

	private void addClickTask(ScheduledTask runnableWrapper) {
		executedTaskBuffer.add(runnableWrapper);
	}

	@Override
	public double getHypertime() {
		return this.hypertime;
	}

	@Override
	public long getNow() {
		return (long)(this.start + ((time.getTimeOfDay() - this.realstart) * this.hypertime));
	}

        @Override
        public long getTime() {
            return this.getNow() - Text.getToday();
        }

	@Override
	public Future<?> schedule(String key, long millis, Runnable task) {		
		ScheduledTask wrapper = new ScheduledTask(key,task,this, this.hypertime);		
		millis = this.getRealtime(millis);
		ScheduledFuture<?> v = this.timer.schedule(wrapper, millis, this.timeUnit);
		wrapper.setFuture(v);
		return v;
	}


	/**
	 * Get the realtime schedule delay for a delay in pseudo time millis.
	 * e.g. hypertime of 10, 1000 millis sent, realtime = 100 millis.
	 * @param interval
	 * @return
	 */
	private long getRealtime(long interval) {
		return (long)(interval / this.hypertime);
	}
	private long getRealtime(Date date) {
		return this.getRealtime(date.getTime()-time.getTimeOfDay());
	}
	@Override
	public Future<?> schedule(String key,Date date, Runnable task) {
		ScheduledTask wrapper = new ScheduledTask(key,task,this, this.hypertime);		
		ScheduledFuture<?> v = this.timer.schedule(wrapper, this.getRealtime(date),this.timeUnit);
		wrapper.setFuture(v);
		return v;
	}

	@Override
	public Future<?> schedule(String key,Date date, long thenevery, Runnable task) {
		ScheduledTask wrapper = new ScheduledTask(key, task,this, this.hypertime);		
		ScheduledFuture<?> v = this.timer.scheduleWithFixedDelay(wrapper, this.getRealtime(date),this.getRealtime(thenevery), this.timeUnit);
		wrapper.setFuture(v);
		return v;
	}

	@Override
	public Future<?> schedule(String key, long initial, long every, Runnable task) {
		ScheduledTask wrapper = new ScheduledTask(key,task,this, this.hypertime);
		ScheduledFuture<?> v = this.timer.scheduleWithFixedDelay(wrapper, this.getRealtime(initial),this.getRealtime(every), this.timeUnit);
		wrapper.setFuture(v);
		return v;
	}

	@Override
	public void setHypertime(double multiplier) {
		if (multiplier == 0) {
			throw new IllegalArgumentException("Invalid multiplier: 0");
		}
		//double previousHyperTime = this.hypertime;
		this.hypertime = multiplier;
        //NOTE: already scheduled tasks will run with existing hypertime value. no re-allocation attempt will be made.
	}

	@Override
	public void setStartTime(long now) {
		this.start = now;
	}
	
	@Override
	public void start() {
		super.start();
		this.realstart = time.getTimeOfDay();
		this.timer = new ScheduledThreadPoolExecutor(1,new ThreadFactory(){

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("SchedulerThread");
				return t;
			}});
        this.start = this.realstart;
        this.timer.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        this.timer.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
	}

    @Override
	public void stop() {
		this.timer.shutdown();
		this.timer = null;
		super.stop();
	}

	@Override	
	public synchronized void next(boolean playMissed, boolean block) throws InterruptedException {
		ScheduledTask wrapper = null;
		/*
		 * first dispatch events on the click queue (those that have tried to execute but as
		 * we are in this mode have been queued up). Then when those are exhausted take the 
		 * tasks off the front of the queue.
		 */
		//synchronized (this.clickList) {
			if (this.executedTaskBuffer.size() > 0) {
				if (!playMissed) {
					this.executedTaskBuffer.clear();
				} else {
					wrapper = this.executedTaskBuffer.remove(0);
				}
				if (wrapper != null) {
					wrapper.doRun();
				} else {
                    if (block) {
                        wrapper = ((ScheduledTask)this.timer.getQueue().take()); 
                    } else {
					    wrapper = ((ScheduledTask)this.timer.getQueue().poll());  //no blocking
                    }
				}
			} else {
                if (block) {
                    wrapper = ((ScheduledTask)this.timer.getQueue().take());
                } else {
                    wrapper = ((ScheduledTask)this.timer.getQueue().poll());
                }
            }
		//}
		if (wrapper != null) {
			wrapper.doRun();
		}
		
	}
	
	@Override
	public synchronized void pause() {
		if (this.isPaused()) { 
			return;
		}
		super.pause();
	}
	
	@Override
	public void resume() {
		this.resume(true);
	}

    /**
     * resume the playback of tasks, running any ones that we may have missed since pausing (non click mode)
     * @param playMissedTasks
     */
	public synchronized void resume(boolean playMissedTasks) {
		if (!this.isPaused()) {
			return;
		}
		/*
		 * unpausing realtime mode. play all events in buffer.
		 */
		super.resume();
		if (!isClickMode()) {
			if (playMissedTasks) {
				for (ScheduledTask task: this.executedTaskBuffer) {
					task.doRun();
				}
			} else {
				this.executedTaskBuffer.clear();
			}
		}
	}
	
	public synchronized boolean isClickMode() {
		return clickMode;
	}
    public void boot() {
        initialise();
        start();
    }
	/**
	 * Change mode between realtime/hypertime and click mode. If going from click to
	 * realtime you have the option to ignore or play the currently buffered tasks.
	 * @param clickMode
	 */
	@Override
	public synchronized void setClickMode(boolean clickMode) {
		if (this.clickMode == clickMode) {
			return;
		}
		this.clickMode = clickMode;

//		if (!this.clickMode) {
//			if (playMissedTasks) {
//				for (ScheduledTask task: this.executedTaskBuffer) {
//					task.doRun();
//				}
//			} else {
//				this.executedTaskBuffer.clear();
//			}
//		}
	}

    /**
     * Set the time unit scheduling will operate under.
     * @param timeUnit
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Return the currently specified time unit this scheduler is operating under.
     *
     * @return
     */
    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }
}
