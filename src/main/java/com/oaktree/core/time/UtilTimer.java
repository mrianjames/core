package com.oaktree.core.time;

import com.oaktree.core.utils.Text;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A basic timer based on a java util timer.
 * @author ij
 *
 */
public final class UtilTimer extends Timer implements ITimer {

    private ITime time = new JavaTime();
    public UtilTimer() {}
    public UtilTimer(ITime time) {
        this.time = time;
    }

	@Override
	public long getMidnight() {
		return Text.getToday();
	}

	@Override
	public long getNow() {
		return time.getTimeOfDay();
	}

	@Override
	public long getTime() {
		return getNow()-getMidnight();
	}

	@Override
	public void schedule(final Runnable task, long date) {
		TimerTask t = null;
		if (task instanceof TimerTask) {
			t = (TimerTask)task;
		} else {
			t = new TimerTask() {

				@Override
				public void run() {
					task.run();
				}				
			};
		}
		this.schedule(t,date);
	}

	@Override
	public void schedule(final Runnable task, Date date) {
		TimerTask t = null;
		if (task instanceof TimerTask) {
			t = (TimerTask)task;
		} else {
			t = new TimerTask() {

				@Override
				public void run() {
					task.run();
				}				
			};
		}
		this.schedule(t,date);
	}

}
