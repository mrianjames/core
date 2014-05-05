package com.oaktree.core.syslog;

import com.oaktree.core.utils.Text;

public class Facility {
	private String desc;
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int id;

	public Facility(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}
	public String toString() {
		return id + Text.SPACE + desc;
	}
}