package com.oaktree.core.container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.memory.IProcessStatistics;
import com.oaktree.core.time.ITimeScheduler;
import com.oaktree.core.utils.Text;

/**
 * A recorder and printer of statistics that can be used by a system or
 * component for measuring msg counts. listeners can be attached to receive
 * threshold breaches (warning/error or exception levels can be set).
 * 
 * @author ij
 * 
 */
public class MsgCounter extends AbstractComponent implements Runnable {

	private int warningThreshold = 0;
	private int errorThreshold = 0;
	/*
	 * TODO throwing exception.
	 */
	private int exceptionThreshold = 0;

	private List<IMsgCounterThresholdListener> thresholdListeners = new ArrayList<IMsgCounterThresholdListener>();

	public void setThresholdListener(IMsgCounterThresholdListener listener) {
		thresholdListeners.add(listener);
	}

	public void addThresholdListener(IMsgCounterThresholdListener listener) {
		thresholdListeners.add(listener);
	}

	public int getWarningThreshold() {
		return warningThreshold;
	}

	public void setWarningThreshold(int warningThreshold) {
		this.warningThreshold = warningThreshold;
	}

	public int getErrorThreshold() {
		return errorThreshold;
	}

	public void setErrorThreshold(int errorThreshold) {
		this.errorThreshold = errorThreshold;
	}

	public int getExceptionThreshold() {
		return exceptionThreshold;
	}

	public void setExceptionThreshold(int exceptionThreshold) {
		this.exceptionThreshold = exceptionThreshold;
	}

	private final static Logger logger = LoggerFactory.getLogger(MsgCounter.class.getName());
	/*
	 * counter for working out when to print the stats
	 */
	private int print = 0;
	/**
	 * Although we collect stats every statsDuration we will print every n
	 * durations.
	 */
	private int printStatsEvery = 10;
	/**
	 * flag to turn on or off recording/printing.
	 */
	private boolean recordStats;
	/**
	 * duration between stats collections. normally a second. period in
	 * milliseconds
	 */
	private int statsDuration = 1000;
	/**
	 * counter for per timerperiod stats.
	 */
	private AtomicLong count = new AtomicLong(0);
	// where we will hold the stats
	private DescriptiveStatistics stats = new DescriptiveStatistics();
	
	private ITimeScheduler scheduler;
	private Future<?> appointment;
	private IProcessStatistics proc;


	public void setScheduler(ITimeScheduler scheduler) {
		this.scheduler = scheduler;
	}

	/**
	 * Increment incoming msg count if recording is on.
	 */
	public void incrementCount() {
		this.count.incrementAndGet();
	}

	@Override
	public void run() {
		if (this.recordStats) {
			long cnt = this.count.get();
			this.stats.addValue(cnt);
			if (this.exceptionThreshold > 0 && cnt >= this.exceptionThreshold) {
				for (IMsgCounterThresholdListener listener:this.thresholdListeners) {
					listener.onMsgCounterExceptionThreshold(this, cnt);
				}
			} else if (this.errorThreshold > 0 && cnt >= this.errorThreshold) {
				for (IMsgCounterThresholdListener listener:this.thresholdListeners) {
					listener.onMsgCounterErrorThreshold(this, cnt);
				}
			} else if (this.warningThreshold > 0 && cnt >= this.warningThreshold) {
				for (IMsgCounterThresholdListener listener:this.thresholdListeners) {
					listener.onMsgCounterWarningThreshold(this, cnt);
				}
			}
		} else {
			// timer still running but not doing stats currently.
			return;
		}
		//if printStatsEvery of 0, we dont print at all...just record and trip
		//breach thresholds if applicable.
		if (printStatsEvery > 0) {
			print += 1;
			if (print == printStatsEvery) {
				print = 0;
				if (logger.isInfoEnabled()) {
					double med = this.stats.getPercentile(50);
					if (!Double.isNaN(med)) {
						logger.info(this.getName() + " current: " + this.count.get() + " Avg:" + Text.to2Dp(med) + " 90%:"
								+ Text.to2Dp(this.stats.getPercentile(90)) + " Peak:" + Text.to2Dp(this.stats.getMax()) + " Total: " + Text.to2Dp(this.stats.getSum()));
					}
				}
			}
		}
		if (this.recordStats) {
			this.count.set(0);
		}

	}
	
	public void setProcessStatistics(IProcessStatistics proc) {
		this.proc = proc;
	}

	public int getPrintStatsEvery() {
		return printStatsEvery;
	}

	public void setPrintStatsEvery(int printStatsEvery) {
		this.printStatsEvery = printStatsEvery;
	}

	public boolean isRecordStats() {
		return recordStats;
	}

	public void setRecordStats(boolean recordStats) {
		this.recordStats = recordStats;
	}

	public int getStatsDuration() {
		return statsDuration;
	}

	public void setStatsDuration(int statsDuration) {
		this.statsDuration = statsDuration;
	}


	@Override
	public void start() {
		super.start();
		if (this.scheduler == null) {
			throw new IllegalStateException("Invalid setup, no timer");
		}
		this.appointment = this.scheduler.schedule(getName(),this.statsDuration, this.statsDuration,this);
	}

	@Override
	public void stop() {
		super.stop();
		if (this.appointment != null) {
			this.appointment.cancel(false);
		}
	}

}
