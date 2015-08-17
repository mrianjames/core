package com.oaktree.core.file;

import com.oaktree.core.time.Precision;
import com.oaktree.core.time.Timestamp;
import com.oaktree.core.time.TimestampFormat;
import com.oaktree.core.time.TimestampUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A timestamped file is a wrapper around a random access file where lines
 * timestamped lines can be parsed and searched.
 * Consider an application which produces a large file, full of useful logging. The file is in order and lines have a known
 * format of timestamp. It should be perfectly possible to have a tool that can efficiently parse such a file such that
 * you can pick out lines for further inspection within supplied timestamps.
 *
 * Created by ij on 06/08/15.
 */
public class TimestampedFile {

    private final String fileName;
    private RandomAccessFile file = null;
    private String firstLine;
    private String lastLine;
    private TimestampFormat timestampFormat;
    private Timestamp lLastTimestamp;
    private String strFirstTimestamp;
    private String strLastTimestamp;
    private Timestamp lFirstTimestamp;

    /**
     * Create a timestamp stamped file. The positioning of the timestamp and timestampFormat is defined by:
     * "****************** hh:mm:ss.iiiuuunnn".
     * @param fileName
     * @param format
     */
    public TimestampedFile(String fileName, String format) {
        this.fileName = fileName;
        parseFormat(format);
        try {
            this.file = new RandomAccessFile(fileName,"r");
            this.refresh();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public TimestampFormat getTimestampFormat() {
        return this.timestampFormat;
    }

    public void close() {
        try {
            this.file.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    RandomAccessFile getFile() {
        return file;
    }

    ;

    private void parseFormat(String format) {
        this.timestampFormat = new TimestampFormat(format);
    }

    /**
     * Reload the key shortcut elements.
     */
    public void refresh() {
        this.firstLine = getFirstLine();
        this.lastLine = getLastLine();
        lFirstTimestamp = timestampFormat.asTimestamp(firstLine);
        lLastTimestamp = timestampFormat.asTimestamp(lastLine);
//        strFirstTimestamp = firstLine.substring(0, 8);
//        strLastTimestamp = lastLine.substring(0, 8);
//        lFirstTimestamp = TimestampUtils.strSecondTimeToTimestamp(strFirstTimestamp, Precision.Nanos);
//        lLastTimestamp = TimestampUtils.strSecondTimeToTimestamp(strLastTimestamp, Precision.Nanos);
    }

    /**
     * Get the fist line in this file.
     * @return
     */
    public String getFirstLine() {
        try {
            file.seek(0);
            return file.readLine();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the last line in this file.
     * @return
     */
    public String getLastLine() {
        try {
            file.seek(file.length()-2);
            return getPreviousLineFromSeekPos();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the line commencing before current file position.
     *
     * @return the previous line from the current position.
     */
    public String getPreviousLineFromSeekPos()  {
        try {
            long pos = file.getFilePointer();
            char c = (char) file.read();
            while (c != '\n' && c != '\r') {
                pos = pos - 1;
                if (pos <= 0) {
                    return null;
                }
                file.seek(pos);
                c = (char) file.read();
            }
            return file.readLine();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the position of the next line from current file position.
     *
     * @return the next line from the current position.
     */
    public long getNextLinePosFromSeekPos() {
        try {
            char c = (char)file.read();
            while (c != '\n' && c!='\r') {
                if (file.getFilePointer() >= file.length()) {
                    return -1;
                }
                c = (char)file.read();
            }
            return file.getFilePointer();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * For current file position walk forwards until you encounter an EOL and return the
     * next line.
     *
     * @return
     */
    public String getNextLineFollowingSeekPos()  {
        try {
            char c = (char)file.read();
            while (c != '\n' && c!='\r') {
                if (file.getFilePointer() >= file.length()) {
                    return null;
                }
                c = (char)file.read();
            }
            return file.readLine();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the position of the previous line from file position.
     * Checks the current position - if its EOL then returns that position.
     * For anything else it walks backwards till it finds an EOL.
     * EOL can be \n or \r.
     *
     * @return
     */
    public long getPreviousLinePosFromSeekPos()  {
        try {
            long pos = file.getFilePointer();
            char c = (char)file.read();
            while (c != '\n' && c!='\r') {
                pos = pos-1;
                if (pos <= 0) {
                    return -1;
                }
                file.seek(pos);
                c = (char)file.read();
            }
            return file.getFilePointer();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @param lowerCheckedPos
     * @param upperCheckedPos
     * @param posToCheck
     * @param searchTimestamp
     * @return
     */
    public long seekForFirstInstanceOfTimestamp(double lowerCheckedPos, double upperCheckedPos, long posToCheck, Timestamp searchTimestamp) {
        try {
            if (posToCheck == file.length()) {
                file.seek(posToCheck - 2);
            } else {
                file.seek(posToCheck);
            }
            String line = getPreviousLineFromSeekPos();
            //Timestamp previousLineTimestamp = TimestampUtils.strSecondTimeToTimestamp(line.substring(0, 8), Precision.Nanos);
            Timestamp previousLineTimestamp = getTimestampFromString(line);
            Precision leastPrecision = TimestampUtils.getLeastGranularPrecision(searchTimestamp, previousLineTimestamp);

            long seekPos = 0;
            long step = 0;
            if (searchTimestamp.equals(previousLineTimestamp,leastPrecision)) {
                //great news. we found our search timestamp. but still need to go backwards to find the first instance of this...
                upperCheckedPos = posToCheck;
                step = (long) ((upperCheckedPos - lowerCheckedPos) / 2.0);
                if (step == 0) {
                    //fantastic...
                    file.seek(posToCheck); //check
                    return getPreviousLinePosFromSeekPos();
                } else {
                    posToCheck = posToCheck - step;
                    //we need to go back further in time...
                    seekPos = seekForFirstInstanceOfTimestamp( lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
                }
            } else if (searchTimestamp.isBefore(previousLineTimestamp,leastPrecision)) {
                upperCheckedPos = posToCheck;
                step = (long) ((upperCheckedPos - lowerCheckedPos) / 2.0); //TODO think rounding issue here...
                posToCheck = posToCheck - step;
                //need to go back further in time
                seekPos = seekForFirstInstanceOfTimestamp(lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
            } else if (searchTimestamp.isAfter(previousLineTimestamp,leastPrecision)) {
                lowerCheckedPos = posToCheck;
                step = (long) (upperCheckedPos - lowerCheckedPos);
                posToCheck = posToCheck + step;
                //go forward in time...
                seekPos = seekForFirstInstanceOfTimestamp(lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
            }
            return seekPos;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Move through a file looking for where a timestamp is in file.
     * @param lMyTimestamp
     * @param isStart
     * @return
     */
    public long seek(Timestamp lMyTimestamp,boolean isStart) {
        try {
            Precision leastPrecision = TimestampUtils.getLeastGranularPrecision(lMyTimestamp,lFirstTimestamp);
            //TODO best to do timestamp stuff in dedicated function/formatter...
            //Timestamp lMyTimestamp = TimestampUtils.strSecondTimeToTimestamp(timestamp.substring(0, 8), Precision.Nanos);
            if (isStart && lMyTimestamp.equals(lFirstTimestamp, leastPrecision)) { //TODO do we need equals(stamp,comparingPrecision).
                return 0;
            }
            if (!isStart && lMyTimestamp.equals(lLastTimestamp, leastPrecision)) {
                return file.length();
            }
            //TODO doesnt really matter what these are in..but obviously all have to use same precision base for comparison...
            long lDuration = lLastTimestamp.getDifference(lFirstTimestamp, leastPrecision);
                   // (lLastTimestamp.getTimestamp() - lFirstTimestamp.getTimestamp()) / 1000000000;
            //long secondsFromStart = (lMyTimestamp.getTimestamp() - lFirstTimestamp.getTimestamp()) / 1000000000;
            long secondsFromStart = -(lFirstTimestamp.getDifferenceAndConvertIfRequired(lMyTimestamp));
            double pct = (1.0 / lDuration) * secondsFromStart;
            file.seek(file.length());
            if (isStart) {
                if (lMyTimestamp.isAfter(lLastTimestamp,leastPrecision)) {
                    return -1;
                }
                if (lMyTimestamp.isBefore(lFirstTimestamp,leastPrecision)) {
                    return 0;
                }
                return seekForFirstInstanceOfTimestamp(0, file.getFilePointer(), (long) (pct * file.getFilePointer()), lMyTimestamp);
            } else {
                //TODO isBefore to a precision, like equals.
                if (lMyTimestamp.isBefore(lFirstTimestamp,leastPrecision)) {
                    return -1;
                }
                if (lMyTimestamp.isAfter(lLastTimestamp,leastPrecision)) {
                    return file.getFilePointer();
                }
                return seekForLastInstanceOfTimestamp(0, file.getFilePointer(), (long) (pct * file.getFilePointer()), lMyTimestamp);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Search for the last instance in the file of a timestamp.
     * @param lowerCheckedPos
     * @param upperCheckedPos
     * @param posToCheck
     * @param searchTimestamp
     * @return
     */
    public long seekForLastInstanceOfTimestamp(double lowerCheckedPos, double upperCheckedPos, long posToCheck, Timestamp searchTimestamp){
        try {
            file.seek(posToCheck);
            String line = getNextLineFollowingSeekPos();
            //Timestamp nextLineTimestamp = TimestampUtils.strSecondTimeToTimestamp(line.substring(0, 8), Precision.Nanos);
            Timestamp nextLineTimestamp = getTimestampFromString(line);
            Precision leastPrecision = TimestampUtils.getLeastGranularPrecision(searchTimestamp, nextLineTimestamp);

            long seekPos = 0;
            long step = 0;
            if (searchTimestamp.equals(nextLineTimestamp,leastPrecision)) {
                lowerCheckedPos = posToCheck;
                step = (long) ((upperCheckedPos - lowerCheckedPos) / 2.0);
                if (step == 0) {
                    file.seek(file.getFilePointer() - 2); //TODO right.?..
                    return file.getFilePointer();
                } else {
                    posToCheck = posToCheck + step;
                    //we need to go further ahead in time...
                    seekPos = seekForLastInstanceOfTimestamp(lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
                }

            } else if (searchTimestamp.isAfter(nextLineTimestamp,leastPrecision)) {
                lowerCheckedPos = posToCheck;
                step = (long) ((upperCheckedPos - lowerCheckedPos) / 2.0);
                posToCheck = posToCheck + step;
                //go forward in time.
                seekPos = seekForLastInstanceOfTimestamp(lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
            } else if (searchTimestamp.isBefore(nextLineTimestamp,leastPrecision)) {
                upperCheckedPos = posToCheck;
                step = (long) ((upperCheckedPos - lowerCheckedPos) / 2.0);
                posToCheck = posToCheck - step;
                //we need to go back in time..
                seekPos = seekForLastInstanceOfTimestamp(lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
            }

            return seekPos;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Get the first position for a timestamp.
     * @param timestamp
     * @return
     */
    public long getSeekPositionForStartTimestamp(Timestamp timestamp) {
        return seek( timestamp, true);
    }

    /**
     * Get the position for the first instance of an end timestamp.
     * @param timestamp
     * @return
     * @throws IOException
     */
    public long getSeekPositionForEndTimestamp(Timestamp timestamp) {
        return seek(timestamp, false);
    }

    /**
     * Get all lines within a time range in a simple timestampFormat. We convert these times into a Timestamp object
     * which accurately represents the time and precision.
     */
    public Collection<String> getAllLinesWithin( String startTime, String endTime)  {
        try {
            Timestamp startTs = getTimestampFromPartialString(startTime);
            Timestamp endTs = getTimestampFromPartialString(endTime);
            List<String> lines = new ArrayList<String>();

            long startPos = getSeekPositionForStartTimestamp(startTs);
            long endPos = getSeekPositionForEndTimestamp(endTs);
            if (endPos == -1) {
                endPos = file.length();
            }
            file.seek(startPos);
            do {
                String line = file.readLine();
                if (lineFilter != null) {
                    Object[] data = timestampFormat.split(line);
                    if (lineFilter.filter((Timestamp)data[0],(String)(data[1]))) {
                        lines.add(line);
                    }
                } else {
                    lines.add(line);
                }
            } while (file.getFilePointer() < endPos);
            return lines;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private ITimestampLineFilter lineFilter;
    public void setLineFilter(ITimestampLineFilter filter) {
        this.lineFilter = filter;
    }

    /**
     * Convert a string timestamp into a proper timestamp object we can use to
     * parse the file.
     * @param strTime
     * @return
     */
    public Timestamp getTimestampFromPartialString(String strTime) {
        return timestampFormat.asTimestampFromPartial(strTime);
    }

    public Timestamp getTimestampFromString(String strTime) {
        return timestampFormat.asTimestamp(strTime);
    }
}
