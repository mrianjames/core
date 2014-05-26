package com.oaktree.core.gc;

import com.oaktree.core.utils.Text;

//name is ParNew
public class GCEvent {
    private final String cause;
    private final String action;
    private long stime;
    private long etime;
	private String name;
	private long before;
	private long after;
    private String memoryArea;

	public GCEvent(String memoryArea,long stime, long etime, String name,String action, String cause, long before, long after) {
        this.memoryArea = memoryArea;
		this.stime = stime;
        this.etime = etime;
		this.name = name;
		this.before = before;
		this.after = after;
        this.cause = cause;
        this.action = action;
	}

	public long getStartTime() {
		return stime;
	}
    public long getEndTime() { return etime; }

	public String getName() {
		return name;
	}


	public long getBefore() {
		return before;
	}

	public void setBefore(long before) {
		this.before = before;
	}

	public long getAfter() {
		return after;
	}

	public void setAfter(long after) {
		this.after = after;
	}

    public String getCause() { return this.cause; }

    public String getAction() { return this.action; }

    public String getMemoryArea() { return this.memoryArea; }

    public long getDuration() { return this.etime - this.stime; }

    public long getCollectionSizeBytes() { return after - before; }

    public double getCollectionSizeK() { return getCollectionSizeBytes()/1024d;}

    public double getCollectionSizeMB() { return getCollectionSizeK()/1024d; }

    public double getAfterUsedSizeK() { return (double)after/1024d; }
    public double getBeforeUsedSizeK() { return (double)before/1024d; }
    public double getAfterUsedSizeMB() { return getAfterUsedSizeK()/1024d; }
    public double getBeforeUsedSizeMB() { return getBeforeUsedSizeK()/1024d; }

    @Override
    public String toString() {
        return memoryArea+ " " +Text.renderTime(stime/1000) + " " +name + " " + action + " "+ cause + " duration:" + (etime-stime) + "us "+getCollectionSizeK() + "K was "+getBeforeUsedSizeK()+ "K now: " + (getAfterUsedSizeK())+"K";
    }
}
