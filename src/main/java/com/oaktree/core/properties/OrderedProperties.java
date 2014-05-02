package com.oaktree.core.properties;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

/**
 * This class acts as a std Properties however order is maintained as underlying colleciton
 * is a linkedhashmap.
 * 
 * @author IJ
 *
 */
@SuppressWarnings("serial")
public class OrderedProperties extends Properties {
	
	private Map<Object,Object> map =  Collections.synchronizedMap(new LinkedHashMap<Object,Object>());
	
    public OrderedProperties() {
        super(null);
    }
    
	@Override
	public Object put(Object key, Object value) {
		return map.put(key, value);		
	}
	
	public Object get(Object key) {
		return map.get(key);		
	}
	
	@Override
	public String getProperty(String key) {
		return (String)map.get(key);
	}
	
	@Override
	public String getProperty(String key,String defaultValue) {
		String v = (String)map.get(key);
		if (v == null) {
			v = defaultValue;
		}
		return v;
	}
	
	@Override
	public void clear() {
		map.clear();
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public boolean contains(Object value) {		
		return map.containsValue(value);
	}
	@Override
	public boolean containsKey(Object key) {		
		return map.containsKey(key);
	}
	@Override
	public boolean containsValue(Object value) {		
		return map.containsValue(value);		
	}
	
	@Override
	public boolean isEmpty() {
		return size() == 0;		
	}
	
	@Override
	public Set<Map.Entry<Object, Object>> entrySet() {
		return new LinkedHashSet<Map.Entry<Object, Object>>(map.entrySet());
	}

	@Override
	public boolean equals(Object a) {
		return super.equals(a);
	}
	
	@Override
	 public Enumeration<Object> elements() {
		 return new Vector<Object>(this.map.keySet()).elements();		
	 }
	 
	@Override
	 public Object remove(Object key) {
		 return map.remove(key);		 
	 }
	
	@Override
	public void list(PrintStream out) {
		for (Map.Entry<Object,Object> es:map.entrySet()) {
			out.print(es.getKey()+"="+es.getValue()+",");
		}
		out.append("\n");
	}
	
	@Override
	public void list(PrintWriter out) {
		for (Map.Entry<Object,Object> es:map.entrySet()) {
			out.print(es.getKey()+"="+es.getValue()+",");
		}
		out.append("\n");
	}
	
}
