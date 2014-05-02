package com.oaktree.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * A utility for recording and presenting meaningful sample timings. Uses apache descriptivestatistics to calc the
 * various stats. Example usage:
 * 
 * <pre>
 * 
 * int TESTS = 10000;
 * ResultTimer timer = new ResultTimer(1000);
 * for (int i = 0; i &lt; TESTS; i++) {
 * 	timer.startSample();
 * 	double ty = 56 * 34 / 23 / 2 * 2;
 * 	timer.endSample();
 * }
 * 
 * System.out.println(timer);
 * </pre>
 * 
 * @author jameian
 * 
 */
public final class ResultTimer {

    private ITime time = new JavaTime();

    public void setTime(ITime time) {
        this.time = time;
    }

    void add(double v) {
    	if (ignoreCount >= ignoreFirst) {
			stats.addValue(v);
		} else {
			ignoreCount++;
		}    	    	
    }
    /**
	 * How many initial samples to ignore before we start recording?
	 */
	private int ignoreFirst;

	/**
	 * formatter. 3 dp.
	 */
	private DecimalFormat format = new DecimalFormat("#,###.####");

	private static final double NANOS_IN_SECOND = 1000000000;
	private static final double NANOS_IN_MILLISECOND = 1000000;
	private static final double NANOS_IN_MICROSECOND = 1000;
	private static final BigDecimal NANOS_IN_HOUR = new BigDecimal("3600000000000");
	private static final BigDecimal NANOS_IN_MINUTE = new BigDecimal("60000000000");

	/**
	 * The maths bit.
	 */
	private DescriptiveStatistics stats = new DescriptiveStatistics();

	/**
	 * Time of the first sample start, including ignored ones. Used for 
	 * duration.
	 */
	private long firstStart;
	/**
	 * Current sample start
	 */
	private long sampleStart;
	/**
	 * Current (or last) sample end.
	 */
	private long sampleEnd;
	/**
	 * How many samples have we ignored currently?
	 */
	private int ignoreCount;

	public ResultTimer() {
	}
	
	public double[] getValues() {
		return stats.getValues();
	}
	
	public void add(ResultTimer timer) {
		//add other stats to this one
		for (double d:timer.getValues()) {
			if (ignoreCount >= ignoreFirst) {
				stats.addValue(d);
			} else {
				ignoreCount++;
			}			
		}
	}

	public DecimalFormat getFormat() {
		return format;
	}

	public void setFormat(DecimalFormat format) {
		this.format = format;
	}

	/**
	 * Create but ignore first n results
	 * 
	 * @param ignoreFirst
	 */
	public ResultTimer(int ignoreFirst) {
		this.ignoreFirst = ignoreFirst;
	}

	/**
	 * Commence the sample timing
	 */
	public void startSample() {
		long t = time.getNanoTime();
		if (firstStart == 0) {
			firstStart = t;
		}
		sampleStart = t;

	}

	/**
	 * End the sample timing
	 */
	public void endSample() {
		sampleEnd = time.getNanoTime();
		if (ignoreCount >= ignoreFirst) {
			stats.addValue(sampleEnd - sampleStart);
		} else {
			ignoreCount++;
		}
	}

	/**
	 * return number of samples
	 * @return num samples
	 */
	public long getCount() {
		return stats.getN();
	}

	/**
	 * Get a percentile, expressed as a unit of time e.g MILLISECONDS.
	 * Pct value is < 100 e.g. median = 50
	 * @param pct
	 * @param tu
	 * @return percentile value.
	 */
	public double getPercentile(double pct, TimeUnit tu) {
		double vnano = stats.getPercentile(pct);
		return nanosToTimeUnit(vnano, tu);
	}

	/**
	 * Get the median sample time expressed in a provided time unit.
	 * @param tu
	 * @return percentile value
	 */
	public double getMedian(TimeUnit tu) {
		return getPercentile(50, tu);
	}

	/**
	 * Get the mean (avg) sample time expressed in a provided time unit.
	 * @param tu
	 * @return mean in specified time unit
	 */
	public double getMean(TimeUnit tu) {
		return nanosToTimeUnit(stats.getMean(), tu);
	}

	/**
	 * Get the jitter (stddev) time expressed in a provided time unit.
	 * @param tu
	 * @return standard deviation in specified time unit.
	 */
	public double getStdDev(TimeUnit tu) {
		return nanosToTimeUnit(stats.getStandardDeviation(), tu);
	}

	/**
	 * Get the variance time in a provided time unit.
	 * @param tu
	 * @return variance in specified time unit
	 */
	public double getVariance(TimeUnit tu) {
		return nanosToTimeUnit(stats.getVariance(), tu);
	}

	/**
	 * Get the least sample time expressed in a provided time unit.
	 * @param tu
	 * @return lowest sample value in specified time unit.
	 */
	public double getMin(TimeUnit tu) {
		return nanosToTimeUnit(stats.getMin(), tu);
	}

	/**
	 * Get the highest sample time expressed in a provided time unit.
	 * @param tu
	 * @return highest sample value in specified time unit.
	 */
	public double getMax(TimeUnit tu) {
		return nanosToTimeUnit(stats.getMax(), tu);
	}

	/**
	 * Convert between nanos and provided time units.
	 * @param value
	 * @param tu
	 * @return nanos converted to specified time unit.
	 */
	private double nanosToTimeUnit(double value, TimeUnit tu) {
		if (tu.equals(TimeUnit.MILLISECONDS)) {
			return value / NANOS_IN_MILLISECOND;
		} else if (tu.equals(TimeUnit.MICROSECONDS)) {
			return value / NANOS_IN_MICROSECOND;
		} else if (tu.equals(TimeUnit.SECONDS)) {
			return value / NANOS_IN_SECOND;
		} else if (tu.equals(TimeUnit.MINUTES)) {
			return new BigDecimal(value).divide(NANOS_IN_MINUTE,RoundingMode.HALF_UP).doubleValue();
		} else if (tu.equals(TimeUnit.HOURS)) {
			return new BigDecimal(value).divide(NANOS_IN_HOUR,RoundingMode.HALF_UP).doubleValue();
		} else if (tu.equals(TimeUnit.NANOSECONDS)) {
			return value;
		} else {
			// unknown...
			return -1;
		}
	}

	/**
	 * time from first test start to last test end.
	 * 
	 * @param tu
	 * @return duration of sample in specified time unit.
	 */
	public double getDuration(TimeUnit tu) {
		return nanosToTimeUnit(sampleEnd - this.firstStart, tu);
	}

	/**
	 * Return a pretty string formatted to specified time unit.
	 * @param tu
	 * @return string representation of this result set in specified time unit.
	 */
	public String toString(TimeUnit tu) {
		return toString(tu,1);
	}

	/**
	 * Return a pretty string formatted to specified time unit.
	 * @param tu
	 * @return string represnetation of this result in specified time unit with all results divided.
	 */
	public String toString(TimeUnit tu, int divisor) {
		return this.getCount() + " elements. " + tu.name() + " Dur:" + format.format(this.getDuration(tu)/divisor) + " Min: "
				+ format.format(this.getMin(tu)/divisor) + " Max: " + format.format(this.getMax(tu)/divisor) + " Avg: "
				+ format.format(this.getMean(tu)/divisor) + " Med: " + format.format(this.getMedian(tu)/divisor) + " 90%:"
				+ format.format(this.getPercentile(90, tu)/divisor) + " 95%:" + format.format(this.getPercentile(95, tu)/divisor)
				+ " 99%:" + format.format(this.getPercentile(99, tu)/divisor) + " StdDev: "
				+ format.format(this.getStdDev(tu)/divisor);
	}

	/**
	 * Return a pretty string, in millis.
	 */
	public String toString() {
		return toString(TimeUnit.MILLISECONDS);
	}


	/**
	 * Remove all values inc ignore counts etc.
	 */
	public void clear() {
		this.stats.clear();
		this.ignoreCount = 0;
		this.firstStart = 0;
	}

}
