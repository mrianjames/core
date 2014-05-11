package com.oaktree.core.data.subscription;

/**
 * Created by ianjames on 06/05/2014.
 */
public class SubscriptionType {
    /**
     * Asynchronous snap (no subscribe)
     */
	public static int ANYTHING = 0; //wild card.
	//bitmasks....
    public static int ASYNC = 1;
    public static int SNAP = 2;
    public static int REALTIME_UPDATES = 4;

    public static boolean isSubscribe(int value) {
        return (value & REALTIME_UPDATES) == REALTIME_UPDATES;  
    }
    public static boolean isSnap(int value) {
        return (value & SNAP) == SNAP;  
    }
    public static boolean isAsync(int value) {
        return (value & ASYNC) == ASYNC;  
    }
    public boolean isAsyncSnap(int value) {
        return isAsync(value)&&isAsync(value);
    }
	public static String toString(int type) {
		String tp = "[";
		if (isAsync(type)) {
			tp += ",ASYNC";
		}
		if (isSnap(type)) {
			tp += ",SNAP";
		}
		if (isSubscribe(type)) {
			tp += ",SUBSCRIBE";
		}
		return tp+"]";
	}
	public static int ASYNC_SNAP_AND_SUBSCRIBE = (ASYNC | SNAP | REALTIME_UPDATES);
}
