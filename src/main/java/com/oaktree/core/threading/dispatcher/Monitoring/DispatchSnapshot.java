package com.oaktree.core.threading.dispatcher.Monitoring;

import com.oaktree.core.utils.Text;

public class DispatchSnapshot {

    public DispatchSnapshot(String name, String clazz,long timestamp, long totalExecCount, long totalQueuedCount, long totalQueuedIncrease,
			long totalExecIncrease, int keyIncrease, double keyIncreaseRate, double queuedIncreaseRate, double execIncreaseRate,
			String topExec, String topQueued, int keyCount) {
		this.timestamp = timestamp;
        this.clazz = clazz;
        this.name = name;
		this.totalExecCount = totalExecCount;
		this.totalQueuedCount = totalQueuedCount;
		this.totalQueuedIncrease = totalQueuedIncrease;
		this.totalExecIncrease = totalExecIncrease;
		this.keyIncrease = keyIncrease;
		this.keyIncreaseRate = keyIncreaseRate;
		this.queuedIncreaseRate = queuedIncreaseRate;
		this.execIncreaseRate = execIncreaseRate;
		this.topExec = topExec;
		this.topQueued = topQueued;
        this.keyCount = keyCount;
	}
    private String name;
    private String clazz;
    public String getName() { return name; }
    public String getClassName() { return clazz; }
    private int keyCount = 0;
	private long timestamp;
	private long totalExecCount;
	private long totalQueuedCount;
	private long totalQueuedIncrease;
	public long totalExecIncrease;
	private int keyIncrease;
	private double keyIncreaseRate;
	private double queuedIncreaseRate;
	private double execIncreaseRate;
	private String topExec;
	private String topQueued;
    public int getKeyCount() {
        return keyCount;
    }
	public long getTimestamp() {
		return timestamp;
	}
	public String getTimestampAsString() {
		return Text.renderTime(timestamp);
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getTotalExecCount() {
		return totalExecCount;
	}
	public void setTotalExecCount(long totalExecCount) {
		this.totalExecCount = totalExecCount;
	}
	public long getTotalQueuedCount() {
		return totalQueuedCount;
	}
	public void setTotalQueuedCount(long totalQueuedCount) {
		this.totalQueuedCount = totalQueuedCount;
	}
	public long getTotalQueuedIncrease() {
		return totalQueuedIncrease;
	}
	public void setTotalQueuedIncrease(long totalQueuedIncrease) {
		this.totalQueuedIncrease = totalQueuedIncrease;
	}
	public long getTotalExecIncrease() {
		return totalExecIncrease;
	}
	public void setTotalExecIncrease(long totalExecIncrease) {
		this.totalExecIncrease = totalExecIncrease;
	}
	public int getKeyIncrease() {
		return keyIncrease;
	}
	public void setKeyIncrease(int keyIncrease) {
		this.keyIncrease = keyIncrease;
	}
	public double getKeyIncreaseRate() {
		return keyIncreaseRate;
	}
    public String getKeyIncreaseRateStr() { return Text.to2Dp(getKeyIncreaseRate());}
	public void setKeyIncreaseRate(double keyIncreaseRate) {
		this.keyIncreaseRate = keyIncreaseRate;
	}
	public double getQueuedIncreaseRate() {
		return queuedIncreaseRate;
	}
    public String getQueuedIncreaseRateStr() { return Text.to2Dp(getQueuedIncreaseRate());};
	public void setQueuedIncreaseRate(double queuedIncreaseRate) {
		this.queuedIncreaseRate = queuedIncreaseRate;
	}
	public double getExecIncreaseRate() {
		return execIncreaseRate;
	}
	public void setExecIncreaseRate(double execIncreaseRate) {
		this.execIncreaseRate = execIncreaseRate;
	}
    public String getExecIncreaseRateStr() { return Text.to2Dp(getExecIncreaseRate());};

    public String getTopExec() {
		return topExec;
	}
	public void setTopExec(String topExec) {
		this.topExec = topExec;
	}
	public String getTopQueued() {
		return topQueued;
	}
	public void setTopQueued(String topQueued) {
		this.topQueued = topQueued;
	}
}
