package com.oaktree.core.file;

import com.oaktree.core.time.*;
import com.oaktree.core.utils.Text;
import org.junit.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

/**
 * Created by ij on 06/08/15.
 */
public class TestTimestampedFile {

    private final static String filename="TEST_FILE";
    private static double EPSILON = 0.00000000000000001;
    private int lines = 8;
    String timestampBase = "13:10:06.23";
    String timestampBaseMore = "13:10:07.02";
    String timestampBaseExtra = "13:10:08.02";
    private TimestampedFile file;

    @Before
    public void setup() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < lines; i++) {
                String string = timestampBase+i+" LINE"+i+"\n";
                bw.write(string);
            }
            bw.flush();
            bw.close();
            file = new TimestampedFile(filename,"hh:mm:ss.iii");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds extra lines to the file so we have a few seconds to do different searches on.
     */
    private void setupExtendedFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
            for (int i = 0; i < lines; i++) {
                //stick extra timestamps in for a more advanced second...
                String string = timestampBaseMore + i + " LINE" + (lines + i) + "\n";
                bw.write(string);
            }
            for (int i = 0; i < lines; i++) {
                //stick extra timestamps in for a more advanced second...
                String string = timestampBaseExtra + i + " LINE" + ((2 * lines) + i) + "\n";
                bw.write(string);
            }
            bw.flush();
            bw.close();
            file.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupLongFile(long base) {
        try {
            tearDown();
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
            for (int i = 0; i < 10000; i++) {
                //stick extra timestamps in for a more advanced second...
                String string = (base+i) + " LINE" + (i) + "\n";
                bw.write(string);
            }

            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupDatedFile() {
        try {
            tearDown();
            String date = "12-AUG-2015 ";
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
            for (int i = 0; i < lines; i++) {
                //stick extra timestamps in for a more advanced second...
                String string = date+timestampBase + i + " LINE" + (i) + "\n";
                bw.write(string);
            }
            for (int i = 0; i < lines; i++) {
                //stick extra timestamps in for a more advanced second...
                String string = date+timestampBaseMore + i + " LINE" + (lines + i) + "\n";
                bw.write(string);
            }
            for (int i = 0; i < lines; i++) {
                //stick extra timestamps in for a more advanced second...
                String string = date+ timestampBaseExtra + i + " LINE" + ((2 * lines) + i) + "\n";
                bw.write(string);
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            file.close();
        } catch (Exception e) {e.printStackTrace();}
        try {
            Path path = FileSystems.getDefault().getPath(filename);
            Files.delete(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Test
    public void testGetLastLine() {
        try {
            String line = file.getLastLine();
            Assert.assertNotNull(line);
            Assert.assertEquals(line, timestampBase + 7 + " LINE" + (lines - 1));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetFirstLine() {
            String line = file.getFirstLine();
            Assert.assertNotNull(line);
            Assert.assertEquals(line, timestampBase + 0 + " LINE0");
    }

    @Test
    public void testGetLineFollowingSeekPos() {
        try {
            file.getFile().seek(5);
            String line = file.getNextLineFollowingSeekPos();
            Assert.assertNotNull(line);
            Assert.assertEquals(line, timestampBase + 1 + " LINE1");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testGetPreviousLineFromSeekPos() {
        try {
            //check what you want from this function.
            long bytesPerLine = file.getFile().length()/lines;
            file.getFile().seek(bytesPerLine+5);
            String line = file.getPreviousLineFromSeekPos();
            Assert.assertNotNull(line);
            Assert.assertEquals(line, timestampBase + 0 + " LINE0");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetLinesWithinAllLines() {
            Collection<String> lines = file.getAllLinesWithin(timestampBase+"0",timestampBase+this.lines);
            Assert.assertEquals(lines.size(), this.lines, EPSILON);
            for (String line:lines) {
                System.out.println(line);
            }
    }

    @Test
    public void testGetLinesWithinCutOutEndOnes() {

            setupExtendedFile();
            file.refresh();
            //get 13:10:06 ->13:10:06 i.e the first minute
            Collection<String> lines = file.getAllLinesWithin((timestampBase + "0").substring(0,8), (timestampBase + this.lines).substring(0,8));
            Assert.assertEquals(lines.size(), this.lines, EPSILON);
            for (String line : lines) {
                System.out.println(line);
            }
    }

    @Test
    public void testGetLinesWithinMiddle() {

            setupExtendedFile();
            //get 13:10:07 ->13:10:07 i.e the first minute
            String min = (timestampBaseMore + "0").substring(0,8);
            Collection<String> lines = file.getAllLinesWithin(min, min);
            Assert.assertEquals(lines.size(), this.lines, EPSILON);
            for (String line : lines) {
                Assert.assertEquals(min,line.substring(0,8));
                System.out.println(line);
            }
            System.out.println("Bravo...");

    }

    @Test
    public void testGetLastMinute() {
            setupExtendedFile();
            //get 13:10:08 ->13:10:08 i.e the last minute
            String min = (timestampBaseExtra + "0").substring(0,8);
            Collection<String> lines = file.getAllLinesWithin(min, min);
            Assert.assertEquals(lines.size(), this.lines, EPSILON);
            for (String line : lines) {
                Assert.assertEquals(min,line.substring(0,8));
                System.out.println(line);
            }
            System.out.println("Bravo...");

    }


    @Test
    public void testDatedTimestamps() {
        setupDatedFile();
        file = new TimestampedFile(filename,"DD-MMM-YYYY hh:mm:ss.iii");
        //get 13:10:08 ->13:10:08 i.e the last minute
        String min = (timestampBaseExtra + "0").substring(0,8);
        Collection<String> lines = file.getAllLinesWithin(min, min);
        Assert.assertEquals(lines.size(), this.lines, EPSILON);
        for (String line : lines) {
            Assert.assertEquals(min,line.substring(12,20));
            System.out.println(line);
        }
        System.out.println("Bravo...");

    }

    @Test
    public void testFilter() {
        final List<String> bits = new ArrayList<String>();
        setupExtendedFile();
        file.setLineFilter(new ITimestampLineFilter() {
            @Override
            public boolean filter(Timestamp timestamp, String line) {
                System.out.println("Timestamp: " + timestamp + ". Line: " + line);
                bits.add(line);
                return true;
            }
        });
        //get 13:10:08 ->13:10:08 i.e the last minute
        String min = (timestampBaseExtra + "0").substring(0,8);
        Collection<String> lines = file.getAllLinesWithin(min, min);
        Assert.assertEquals(lines.size(), this.lines, EPSILON);
        for (String line : lines) {
            Assert.assertEquals(min,line.substring(0,8));
            System.out.println(line);
        }
        Assert.assertEquals(lines.size(),bits.size(),0);
        System.out.println("Bravo...");
    }
//
//    @Test
//    public void testLongTimestamps() {
//        long ms = System.currentTimeMillis();
//        TimeOfDay tod = TimestampUtils.timeOfDayFromTimestamp(new Timestamp(ms*1000*1000,Precision.Nanos));
////        ts.
////        String min = ;
//        //String min=Text.twoDigits(tod.getHour())+":" + Text.twoDigits(tod.getMinute());
//        setupLongFile(ms);
//        String fmt = "iiiiiiiiiiiii";
//        file = new TimestampedFile(filename,fmt);
//        //get 13:10:08 ->13:10:08 i.e the last minute
//        Calendar cal = Calendar.getInstance();
//        cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DATE),(int)tod.getHour(),(int)tod.getMinute(),0);
//        String min = ""+cal.getTime().getTime();
//        Collection<String> lines = file.getAllLinesWithin(min, min);
//        Assert.assertEquals(lines.size(), 10000, EPSILON);
//        long idx = 0;
//        for (String line : lines) {
//            Assert.assertEquals(""+(ms+idx),line.substring(file.getTimestampFormat().getTimeStart(),file.getTimestampFormat().getTimeEnd()+1));
//            System.out.println(line);
//            idx++;
//        }
//        System.out.println("Bravo...");
//
//    }

}
