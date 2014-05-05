package com.oaktree.core.container;

public final class MockFixMessage extends Message {
	public MockFixMessage(String id, String msg) {
		this.id = id;
		this.msg = msg;
	}
	public String id = "";
	public String msg = "";
}