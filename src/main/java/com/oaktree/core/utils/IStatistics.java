package com.oaktree.core.utils;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 08/06/11
 * Time: 12:08
 */
public interface IStatistics {

    public double getMean();

    public double getMedian();

    public double getPercentile(int percentile) ;

    public double getMax();

    public double getVariance();

    public double getStdDev() ;

    public int getCount() ;

    public double getMin();

    public void clear();
}
