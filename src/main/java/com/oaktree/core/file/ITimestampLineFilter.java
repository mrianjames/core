package com.oaktree.core.file;

import com.oaktree.core.time.Timestamp;

/**
 * Created by ij on 17/08/15.
 */
public interface ITimestampLineFilter {
    /**
     * Perform further inspection on a timestamped line. For example, you may know the format of lines
     * within a file e.g. a csv and can breakdown the line into values you check against some criteria.
     *
     *
     * @param timestamp
     * @param line
     * @return
     */
    public boolean filter(Timestamp timestamp, String line);
}
