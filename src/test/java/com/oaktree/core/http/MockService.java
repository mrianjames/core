package com.oaktree.core.http;

import com.oaktree.core.gc.GCEvent;

public class MockService {
	public String getString() {
		return "HELLO";
	}
	public String getStringParam(String param) {
		return param;
	}
	public String[] getData() {
		return new String[]{"A","B","C"};
	}
	
	public GCEvent[] getGCData() {
		return new GCEvent[]{
				new GCEvent("Eden",System.currentTimeMillis(),System.currentTimeMillis()+1,"ParNew","","",1123232332,12312312),
				new GCEvent("Freden",System.currentTimeMillis(),System.currentTimeMillis()+1,"ParNew","","",1123232332,12312312),
				new GCEvent("gEden",System.currentTimeMillis(),System.currentTimeMillis()+1,"ParNew","","",1123232332,12312312),
				new GCEvent("hEden",System.currentTimeMillis(),System.currentTimeMillis()+1,"ParNew","","",1123232332,12312312),
				new GCEvent("aEden",System.currentTimeMillis(),System.currentTimeMillis()+1,"ParNew","","",1123232332,12312312)
				};
	}
}
