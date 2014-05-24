package com.oaktree.core.http;

import com.oaktree.core.gc.GC;

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
	
	public GC[] getGCData() {
		return new GC[]{
				new GC(System.currentTimeMillis(),"ParNew",1123232332,12312312),
				new GC(System.currentTimeMillis(),"ParNew",1123232332,12312312),
				new GC(System.currentTimeMillis(),"ParNew",1123232332,12312312),
				new GC(System.currentTimeMillis(),"ParNew",1123232332,12312312),
				new GC(System.currentTimeMillis(),"ParNew",1123232332,12312312)
				};
	}
}
