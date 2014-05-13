package com.oaktree.core.data.cache;


import com.oaktree.core.data.IData;
import com.oaktree.core.data.cache.DataCache;
import com.oaktree.core.data.cache.IDataCache;

public class TestDataCache {

	private static class TestData implements IData<String> {
		private long data;
		public TestData(long x) {
			this.data = x;
		}
		@Override
		public String getDataKey() {
			return String.valueOf(data);
		}
		
	}
	
	public static void main(String[] args) {
		TestData a = new TestData(12);
		IDataCache<TestData,String> cache = new DataCache<TestData,String>();
		cache.onData(a, null, 100000000);
	}

}
