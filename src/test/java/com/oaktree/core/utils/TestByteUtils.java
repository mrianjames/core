package com.oaktree.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class TestByteUtils {

	@Test
	public void putAndGetStrings() {
		String message1 = "This is a message 1";
		String message2 = "This is a message 2";
		ByteBuffer buffer = ByteBuffer.allocate(1000);
		ByteUtils.putString(message1, buffer);
		ByteUtils.putString(message2, buffer);
		buffer.flip();
		String r1 = ByteUtils.getString(buffer);
		String r2 = ByteUtils.getString(buffer);
		Assert.assertEquals(r1,message1);
		Assert.assertEquals(r2, message2);
	}
}
