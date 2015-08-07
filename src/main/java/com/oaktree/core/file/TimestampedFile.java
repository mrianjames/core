package com.oaktree.core.file;

import com.oaktree.core.time.Precision;
import com.oaktree.core.time.Timestamp;
import com.oaktree.core.time.TimestampUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ij on 06/08/15.
 */
public class TimestampedFile {

    private final String fileName;
    private RandomAccessFile file = null;
    private String firstLine;
    private String lastLine;
    private Format format;
    private Timestamp lLastTimestamp;
    private String strFirstTimestamp;
    private String strLastTimestamp;
    private Timestamp lFirstTimestamp;

    /**
     * Create a timestamp stamped file. The positioning of the timestamp and format is defined by:
     * "****************** HH:MM:SS.sssuuunnn".
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

    /**
     * Structure of our timestamps used in this file.
     * We are only interested in times, but we need to
     * know where the various bits start and what the precision
     * of timestamps will be e.g. min/sec/ms, min/sec/ms/us
     *
     * Example YYYY-MM-DD hh:mm:ss.iiiuuunnn
     */
    private class Format {

        int hourStart = -1;
        int hourEnd = -1;

        int minStart = -1;
        int minEnd = -1;

        int secondStart = -1;
        int secondEnd = -1;

        int milliStart = -1;
        int milliEnd = -1;

        int microStart = -1;
        int microEnd = -1;

        int nanoStart = -1;
        int nanoEnd = -1;

        int timeLength = -1;
        private int timeEnd = -1;

        public boolean hasSecond() {return secondStart > -1;}
        public boolean hasMillis() {return milliStart > -1;}
        public boolean hasMicros() {return microStart > -1;}
        public boolean hasNanos() {return nanoStart > -1;}
        public int getTimeLength() {return getTimeLength();}
        public int getTimeStart() { return hourStart;}
        public int getTimeEnd() { return timeEnd; }
        public int getPrefix() { return hourStart; }
        private char HOURS = 'H';
        private char MINS = 'M';
        private char SECONDS = 'S';
        private char MILLIS = 'i';
        private char MICROS ='u';
        private char NANOS='n';
        public void parse(String format) {
            int lastTime = -1;
            hourStart = format.indexOf(HOURS);
            hourEnd = format.lastIndexOf(HOURS);
            if (hourEnd > -1) { lastTime = hourEnd; }
            minStart = format.indexOf(MINS);
            minEnd = format.lastIndexOf(MINS);
            if (minEnd > -1) { lastTime = hourEnd; }
            secondStart = format.indexOf(SECONDS);
            secondEnd = format.lastIndexOf(SECONDS);
            if (secondEnd > -1) { lastTime = hourEnd; }
            milliStart = format.indexOf(MILLIS);
            milliEnd = format.lastIndexOf(MILLIS);
            if (milliEnd > -1) { lastTime = hourEnd; }
            microStart = format.indexOf(MICROS);
            microEnd = format.lastIndexOf(MICROS);
            if (microEnd > -1) { lastTime = hourEnd; }
            nanoStart = format.indexOf(NANOS);
            nanoEnd = format.lastIndexOf(NANOS);
            if (nanoEnd > -1) { lastTime = hourEnd; }
            this.timeEnd = lastTime;
        }
    };

    private void parseFormat(String format) {
        this.format = new Format();
    }

    /**
     * Reload the key shortcut elements.
     */
    public void refresh() {
        this.firstLine = getFirstLine();
        this.lastLine = getLastLine();
        strFirstTimestamp = firstLine.substring(0, 8);
        strLastTimestamp = lastLine.substring(0, 8);
        lFirstTimestamp = TimestampUtils.strSecondTimeToTimestamp(strFirstTimestamp, Precision.Nanos);
        lLastTimestamp = TimestampUtils.strSecondTimeToTimestamp(strLastTimestamp, Precision.Nanos);
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
            Timestamp previousLineTimestamp = TimestampUtils.strSecondTimeToTimestamp(line.substring(0, 8), Precision.Nanos);
            long seekPos = 0;
            long step = 0;
            if (searchTimestamp.equals(previousLineTimestamp)) {
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
            } else if (searchTimestamp.isBefore(previousLineTimestamp)) {
                upperCheckedPos = posToCheck;
                step = (long) ((upperCheckedPos - lowerCheckedPos) / 2.0); //TODO think rounding issue here...
                posToCheck = posToCheck - step;
                //need to go back further in time
                seekPos = seekForFirstInstanceOfTimestamp(lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
            } else if (searchTimestamp.isAfter(previousLineTimestamp)) {
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
     * @param timestamp
     * @param isStart
     * @return
     */
    public  long seek(String timestamp,boolean isStart) {
        try {
            //TODO best to do timestamp stuff in dedicated function/formatter...
            Timestamp lMyTimestamp = TimestampUtils.strSecondTimeToTimestamp(timestamp.substring(0, 8), Precision.Nanos);
            if (isStart && lMyTimestamp.equals(lFirstTimestamp)) {
                return 0;
            }
            if (!isStart && lMyTimestamp.equals(lLastTimestamp)) {
                return file.length();
            }
            long lDuration = (lLastTimestamp.getTimestamp() - lFirstTimestamp.getTimestamp()) / 1000000000;
            long secondsFromStart = (lMyTimestamp.getTimestamp() - lFirstTimestamp.getTimestamp()) / 1000000000;
            double pct = (1.0 / lDuration) * secondsFromStart;
            file.seek(file.length());
            if (isStart) {
                if (lMyTimestamp.isAfter(lLastTimestamp)) {
                    return -1;
                }
                if (lMyTimestamp.isBefore(lFirstTimestamp)) {
                    return 0;
                }
                return seekForFirstInstanceOfTimestamp(0, file.getFilePointer(), (long) (pct * file.getFilePointer()), lMyTimestamp);
            } else {
                if (lMyTimestamp.isBefore(lFirstTimestamp)) {
                    return -1;
                }
                if (lMyTimestamp.isAfter(lLastTimestamp)) {
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
            Timestamp nextLineTimestamp = TimestampUtils.strSecondTimeToTimestamp(line.substring(0, 8), Precision.Nanos);
            long seekPos = 0;
            long step = 0;
            if (searchTimestamp.equals(nextLineTimestamp)) {
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

            } else if (searchTimestamp.isAfter(nextLineTimestamp)) {
                lowerCheckedPos = posToCheck;
                step = (long) ((upperCheckedPos - lowerCheckedPos) / 2.0);
                posToCheck = posToCheck + step;
                //go forward in time.
                seekPos = seekForLastInstanceOfTimestamp(lowerCheckedPos, upperCheckedPos, posToCheck, searchTimestamp);
            } else if (searchTimestamp.isBefore(nextLineTimestamp)) {
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
    public long getSeekPositionForStartTimestamp(String timestamp) {
        return seek( timestamp, true);
    }

    /**
     * Get the position for the first instance of an end timestamp.
     * @param timestamp
     * @return
     * @throws IOException
     */
    public long getSeekPositionForEndTimestamp( String timestamp) {
        return seek(timestamp, false);
    }

    /**
     * Get all lines within a time range in a simple format.
     */
    public Collection<String> getAllLinesWithin( String startTime, String endTime)  {
        try {
            List<String> lines = new ArrayList<String>();
            long startPos = getSeekPositionForStartTimestamp(startTime);
            long endPos = getSeekPositionForEndTimestamp(endTime);
            if (endPos == -1) {
                endPos = file.length();
            }
            file.seek(startPos);
            do {
                lines.add(file.readLine());
            } while (file.getFilePointer() < endPos);
            return lines;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
