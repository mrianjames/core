package com.oaktree.core.latency;

import org.junit.Assert;
import org.junit.Test;

import com.oaktree.core.id.IIdGenerator;
import com.oaktree.core.id.IdGenerator;

/**
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 10/06/11
 * Time: 08:34
 */
public class TestInMemoryLatencyRecorder {

    @Test
    public void testInMemoryLatencyRecorder() throws InterruptedException {
        InMemoryLatencyRecorder rec = new InMemoryLatencyRecorder();
        rec.setClearOnFlush(true);
        rec.setPrintDuration(1000);
        rec.setMaxElementsPerFlush(2000000);
        rec.initialise();
        rec.start();
        for (int j = 0; j < 60; j ++) {
            for (int i = 0; i < 10000; i++) {
                rec.begin("TYPE","SUB",j+""+i,0);
                rec.end("TYPE", "SUB", j+"" + i, 0);
            }
            Thread.sleep(100);
        }
        Thread.sleep(3000);
        Assert.assertEquals(rec.getNumPendingEvents(),0);
    }

    @Test
    public void testMaxCapacity() {
        InMemoryLatencyRecorder rec = new InMemoryLatencyRecorder();
        rec.setClearOnFlush(false);
        rec.setPrintDuration(1000);
        rec.setMaxElementsPerFlush(10000);
        rec.initialise();
        rec.start();
        for (int i = 0; i < 10000; i++) {
           rec.begin("TYPE","SUB",""+i,0);
           rec.end("TYPE", "SUB", "" + i, 0);
        }
        rec.begin("TYPE","SUB",""+10000,0);
        Assert.assertTrue(rec.isActive());
        rec.end("TYPE", "SUB", "" + 10000, 0);
        Assert.assertFalse(rec.isActive());
        Assert.assertEquals(rec.getNumPendingEvents(),0);
    }
    
    /**
     * A simulation of normal usage for viewing in profiler or jconsole
     * (asses GC)
     * 
     * @param args
     */
    public static void main(String[] args) throws Exception {
    	InMemoryLatencyRecorder rec = new InMemoryLatencyRecorder();
    	rec.setName("TEST LAT_REC");
        rec.setClearOnFlush(true);
        rec.setPrintDuration(1000);
        rec.setDeactivationPrintEvery(1);
        rec.setMaxElementsPerFlush(2000);
        rec.initialise();
        rec.start();
        IIdGenerator idgen =  new IdGenerator("TEST", "IDG");
        String COMP = "TYPE.SUB";
        String SUB = null; 
        while (true) {
        	String id = idgen.next();
        	rec.begin(COMP,SUB,id,0);
        	rec.end(COMP, SUB, id, 0);        	
        	Thread.sleep(1);
        }
        
    }
}
