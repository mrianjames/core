package com.oaktree.core.latency;

import java.io.Writer;

import com.oaktree.core.container.IComponent;

/**
 * A recorder of latency statistics. Records are recorded
 * with various ids to enable correlation and aggregation of
 * events into a "flow" that provides breakdown latencies of
 * an entity through a system.
 * 
 * <pre>
 	int buffersz = 10000; //size of arrays as number of records
	int flushtime = 1000; //time in ms to do flushes.
	FileWriter filewriter = new FileWriter(new File("latency.csv"));
	ILatencyWriter iowriter = new IOLatencyWriter(filewriter,buffersz);
	ILatencyRecorder recorder = new LatencyRecorder(iowriter,buffersz,flushtime);
	recorder.initialise();
	recorder.start();
	...
	recorder.begin("UPSTREAM","NOS","ABC123234",null);
	//or
	long time = System.nanoTime();
	recorder.beginAt("UPSTREAM","NOS","ABC123234",null);
	//do section work
	recorder.begin("UPSTREAM","NOS","ABC123234",null);
	//or
	time = System.nanoTime();
	recorder.endAt("UPSTREAM","NOS","ABC123234",null);
 * </pre>
 * 
 * If using a FileWriter via the IOLatencyWriter then the output may look as follows:<br/>
 * 
 * <pre>
Time,Section,Subsection,ID,SubID,NumericSubID
26871045642043,BEG,ORDER_MAKING,null,_ORD_1272904769979,0
26871053187695,END,ORDER_MAKING,null,_ORD_1272904769979,0
26871132437083,BEG,MATCH,D,_ORD_1272904769979,0
26871133409762,BEG,BEHAVIOUR,D,_ORD_1272904769979,0
26871170297900,END,BEHAVIOUR,D,_ORD_1272904769979,0
26871432169172,END,MATCH,D,_ORD_1272904769979,0
26871432505458,BEG,MD_PUBLISH,D,_ORD_1272904769979,0
26871465013430,END,MD_PUBLISH,D,_ORD_1272904769979,0
</pre>

 * This output tracks an entity (_ORD_1272904769979) through several sections of code - the enties flow goes from ORDER_MAKING->MATCH->MD_PUBLISH and you can therefore derive the total latency for this order as BEG,ORDER_MAKING->END,MD_PUBLISH.
 * @author ij
 *
 */
public interface ILatencyRecorder extends IComponent  {
	/**
	 * Start a sample section for an id. The timestamp will be recorded on commencement of
	 * this method.
	 * @param type - Type of section you are recording
	 * @param subtype - More granular section name.
	 * @param id - Id for the event passing through this section
	 * @param subid - Additional id for the event passing through this section.
	 */
	public void begin(String type, String subtype, String id, long subid);
	/**
	 * Start a sample section for an id, specifying time yourselves (i.e. when the event
	 * actually occurred).
	 * @param type - Type of section you are recording
	 * @param subtype - More granular section name.
	 * @param id - Id for the event passing through this section
	 * @param subid - Additional id for the event passing through this section.
	 * @param time - time in nanoseconds
	 */
	public void beginAt(String type, String subtype, String id, long subid, long time);
	/**
	 * End a sample section for an id. The timestamp will be recorded on commencement of
	 * this method.
	 * @param type - Type of section you are recording
	 * @param subtype - More granular section name.
	 * @param id - Id for the event passing through this section
	 * @param subid - Additional id for the event passing through this section.
	 */
	public void end(String type, String subtype, String id, long subid);
	/**
	 * End a sample section for an id, specifying time yourselves (i.e. when the event
	 * actually occurred).
	 * @param type - Type of section you are recording
	 * @param subtype - More granular section name.
	 * @param id - Id for the event passing through this section
	 * @param subid - Additional id for the event passing through this section.
	 * @param time - time in nanoseconds
	 */
	public void endAt(String type, String subtype, String id, long subid, long time);
	
	/**
	 * Set a writer to write records to.
	 * @param sw
	 */
	public void setWriter(ILatencyWriter sw);
	/**
	 * Is this recorder active.
	 * @return
	 */
	public boolean isActive();
	
}
