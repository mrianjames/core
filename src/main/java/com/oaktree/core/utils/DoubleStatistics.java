package com.oaktree.core.utils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple stats on a number series.
 * Can be used by multiple threads and  queries and multiple write are permitted concurrently
 * so results may be inconsistent to some extent. however for real-world use cases (e.g. latency measurement)
 * this is fine.
 *
 * User: ij
 * Date: 08/06/11
 * Time: 11:52
 */
public class DoubleStatistics implements IStatistics {
	
	private double min = Double.NaN;
    private double max = Double.NaN;

    
	private static class DataSet {
		public DataSet(double[] array, int pos) {
			this.array = array;
			this.pos = pos;
		}
		public double[] array;
		public int pos;
		public double[] getArray() {
			return array;
		}
		public int getPos() {
			return pos;
		}
		public void setPos(int pos) {
			this.pos = pos;
		}
		
		public void clear() {
			pos = 0;
		}
	}
	
	/**
	 * Common set of percentiles we use for latency SLAs. e.g. ensure 99% of operations
	 * complete in x <time units>
	 */
    public static final double[] STD_PERCENTILES = new double[]{50.0,90.0,95.0,99.0};
	private double[] array = new double[0];
    private AtomicInteger pos = new AtomicInteger(0);
    private ThreadLocal<DataSet> tlDataSet;
    
    public DoubleStatistics(final int size) {
        array = new double[size];
        tlDataSet = new ThreadLocal<DataSet>(){
        	@Override 
        	protected DataSet initialValue() {
        		
                return new DataSet(new double[size],0);
        	}
        };
    }

    @Override
    public String toString() {
        StatisticalSummary stats = new StatisticalSummary();
        getAllStatistics(stats);
        return stats.toString();
    }

    /**
     * Add a double value.
     * @param v
     */
    public void addValue(double v) {
        if (pos.get() >= array.length) {
            throw new IllegalStateException("Invalid capacity: " + pos.get());
        }
        if (v < min || Double.isNaN(min)) {
        	min = v;
        }
        if (v > max || Double.isNaN(max)) {
        	max = v;
        }
        array[pos.getAndIncrement()] = v;
    }


    @Override
    public double getMean() {
        DataSet ds = copy();
        return getMean(ds);
    }
    
    public double getMean(DataSet ds) {    	
        return getSum(ds)/(double)ds.getPos();
    }
    public double getMean(DataSet ds,double sum) {    	
        return sum/(double)ds.getPos();
    }
    private double getSum(DataSet ds) {
    	double sum = 0;
    	double[] calcArray = ds.getArray();
        for (int i = 0; i < ds.getPos(); i++) {
            sum += calcArray[i];
        }
        return sum;
	}

	@Override
    public double getMedian() {
        return getPercentile(50);
    }

    @Override
    public double getPercentile(int percentile) {
    	return getPercentile(percentile,true);
    }
    
    public double[] getBulkPercentiles(double[] percentiles) {
    	copyAndSort();
    	double[] results = new double[percentiles.length];
    	int i = 0;
    	for (double pct:percentiles) {
    		results[i] = getPercentile(pct,false);
    		i++;
    	}
    	return results;
    }
    
    public double getPercentile(double percentile,boolean copyAndSort) {
        
        DataSet ds;
        if (copyAndSort) {
        	ds = copyAndSort();
        } else {
        	ds = tlDataSet.get();
        	
        }
        
        return getPercentile(percentile,ds);
        

    }
    
    public void getAllStatistics(StatisticalSummary stats) {
    	stats.clear();
    	DataSet ds = copyAndSort();
    	stats.setCount(ds.getPos());
    	double sum = getSum(ds);
    	double mean = getMean(ds,sum);
    	stats.setMean(mean);
    	stats.setSum(sum);
    	stats.setMax(getMax(ds));
    	stats.setMin(getMin(ds));
    	stats.setMedian(this.getPercentile(50, ds));
    	stats.setNinety(this.getPercentile(90, ds));
    	stats.setNinetyFive(this.getPercentile(95, ds));
    	stats.setNinetyNine(this.getPercentile(99, ds));
    	stats.setNinetyNineAndHalf(this.getPercentile(99.5,ds));
    	double var = this.getVariance(ds,mean);
    	stats.setStddev(this.getStdDev(var));
    	stats.setVariance(var); //mmm, calcs twice.
    }
    
    public double getPercentile(double percentile, DataSet ds) {
    	if (percentile <= 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 1-100 inclusive");
        }
        int p = ds.getPos();
        if (p == 0) {
            return Double.NaN;
        }
        if (p == 1) {
            return ds.getArray()[0];
        }


        double eachp = p/100d;
        int index = (int)(eachp * percentile);
        if (p % 2 == 0) {
            index -= 1;
        }
        return ds.getArray()[index];
    }

    @Override
    public double getMax() {
        return max;
    }
    
    private double getMax(DataSet ds) {
    	if (ds.getPos() == 0) {
    		return Double.NaN;
    	}
    	return ds.getArray()[ds.getPos()-1];
    }
    
    private double getMin(DataSet ds) {
    	if (ds.getPos() == 0) {
    		return Double.NaN;
    	}
    	return ds.getArray()[0];
    }
    

    @Override
    public double getVariance() {
    	DataSet ds = copy();
        double mean = getMean(ds);
        return getDeviationFromMeanProductSum(ds,mean)/ds.getPos();
    }
    
    public double getVariance(DataSet ds,double mean) {
    	return getDeviationFromMeanProductSum(ds,mean)/ds.getPos();
    }
    

    private double getDeviationFromMeanProductSum(DataSet ds, double mean) {
    	double devFromMeanProduct = 0;
        
        for (double v:ds.getArray()) {
            double meanDeviation = v-mean;
            devFromMeanProduct += (meanDeviation*meanDeviation);
        }
        return devFromMeanProduct;
	}

	@Override
    public double getStdDev() {
        return Math.sqrt(getVariance());
    }
    
    public double getStdDev(double variance) {
        return Math.sqrt(variance);
    }

    @Override
    public int getCount() {
        return pos.intValue();
    }
    
    @Override
    public double getMin() {
        return min;
    }

    @Override
    public void clear() {
        pos.set(0);
        min=Double.NaN;
        max=Double.NaN;
    }

    /**
     * Copy the array data to secondary array.
     */
    private DataSet copyAndSort() {
    	DataSet ds = copy();
        double[] calcArray = ds.getArray();
        Arrays.sort(calcArray,0,ds.pos);
        return ds;
    }
    
    private DataSet copy() {
    	int epos = pos.get();
    	DataSet ds = tlDataSet.get();
    	double[] calcArray = ds.getArray();
        System.arraycopy(array, 0, calcArray, 0, epos);
        ds.setPos(epos);
        return ds;
    }
}
