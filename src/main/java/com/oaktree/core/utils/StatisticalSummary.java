package com.oaktree.core.utils;

/**
 * Point in time statistical summary.
 * Non threadsafe. Expected to be updated and printed from a single thread or protected 
 * area. 
 * 
 * @author ij
 *
 */
public class StatisticalSummary {
	
	public StatisticalSummary() {}
	public StatisticalSummary(StatisticalSummary orig) {
		setCount(orig.getCount());
		setMin(orig.getMin());
		setMax(orig.getMax());
		setMean(orig.getMean());
		setMedian(orig.getMedian());
		setNinety(orig.getNinety());
		setNinetyFive(orig.getNinetyFive());
		setNinetyNine(orig.getNinetyNine());
		setNinetyNineAndHalf(orig.getNinetyNineAndHalf());
		setVariance(orig.getVariance());
		setStddev(orig.getStddev());
		setSum(orig.getSum());
	}

	private double max = Double.NaN;
	private double min = Double.NaN;
	private double mean= Double.NaN;
	private double median= Double.NaN;
	private double ninety= Double.NaN;
	private double ninetyFive= Double.NaN;
	private double ninetyNine= Double.NaN;
	private double ninetyNineAndHalf= Double.NaN;
	private double variance= Double.NaN;
	private double stddev= Double.NaN;
	private double sum = Double.NaN;
	public double getSum() {
		return sum;
	}
	public void setSum(double sum) {
		this.sum = sum;
	}
	private int count = 0;
	public int getCount() {
		return count;
	}
	
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}


	public void setCount(int count) {
		this.count = count;
	}


	public double getMax() {
		return max;
	}


	public void setMax(double max) {
		this.max = max;
	}


	public double getMean() {
		return mean;
	}


	public void setMean(double mean) {
		this.mean = mean;
	}


	public double getMedian() {
		return median;
	}


	public void setMedian(double median) {
		this.median = median;
	}


	public double getNinety() {
		return ninety;
	}


	public void setNinety(double ninety) {
		this.ninety = ninety;
	}


	public double getNinetyFive() {
		return ninetyFive;
	}


	public void setNinetyFive(double ninetyFive) {
		this.ninetyFive = ninetyFive;
	}


	public double getNinetyNine() {
		return ninetyNine;
	}


	public void setNinetyNine(double ninetyNine) {
		this.ninetyNine = ninetyNine;
	}


	public double getNinetyNineAndHalf() {
		return ninetyNineAndHalf;
	}


	public void setNinetyNineAndHalf(double ninetyNineAndHalf) {
		this.ninetyNineAndHalf = ninetyNineAndHalf;
	}


	public double getVariance() {
		return variance;
	}


	public void setVariance(double variance) {
		this.variance = variance;
	}


	public double getStddev() {
		return stddev;
	}


	public void setStddev(double stddev) {
		this.stddev = stddev;
	}


	
	public void clear() {
		count = 0;
		max = Double.NaN;
		min= Double.NaN;
		mean= Double.NaN;
		median= Double.NaN;
		ninety= Double.NaN;
		ninetyFive= Double.NaN;
		ninetyNine= Double.NaN;
		ninetyNineAndHalf= Double.NaN;
		variance= Double.NaN;
		stddev= Double.NaN;
		sum = Double.NaN;
	}
	
	/**
	 * Counter for number of additions
	 */
	private int generation = 0;
	/**
	 * Generation we set the min summary value from.
	 */
	private int minGeneration = 0;
	/**
	 * Generation we set the max summary value from.
	 */
	private int maxGeneration = 0;
	/**
	 * Add a summary to this summary. Useful for accumulating basic statistics.
	 * Note that this will do nothing for percentiles. 
	 * @param summary
	 */
	public void add(StatisticalSummary summary) {
		generation++;
		count += summary.getCount();
		if (Double.isNaN(getSum())) {
			sum = summary.getSum();			
		} else {
			sum+=summary.getSum();
		}
		if (summary.getMin() < min || Double.isNaN(min)) {
			min = summary.getMin();
			minGeneration = generation;
		}
		if (summary.getMax() > max || Double.isNaN(max)) {
			max = summary.getMax();
			maxGeneration = generation;
		}
		mean = sum/count;
		/*
		 * TODO stddev/var.
		 */
	}
	/**
	 * Get which generation we set the min from
	 * @return
	 */
	public int getMinGeneration() {
		return minGeneration;
	}
	/**
	 * Get which addition we set the max from.
	 * @return
	 */
	public int getMaxGeneration() {
		return maxGeneration;
	}
	/**
	 * Get current generation we are in.
	 * @return
	 */
	public int getGeneration() {
		return generation;
	}
}
