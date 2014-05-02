package com.oaktree.core.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

/**
 * Blast data at disk and time it. simples.
 * @author ij
 */
public class WriteSpeedTest {
	private static String generatePayload(int chars) {
		StringBuilder b = new StringBuilder(chars);
		for (int i = 0; i < chars; i++) {
			b.append('x');
		}
		return b.toString();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		long LINES = 1000000;
		int MSG_SIZE = 1600; //line size in chars (2 bytes).
		DecimalFormat format = new DecimalFormat("#,###.##");
		double bytes = (LINES*MSG_SIZE);
		System.out.println("Making payload of " + format.format(bytes/1024d/1024d) + "MB.");
		String phrase = generatePayload(MSG_SIZE);
		System.out.println("Commencing write to disk of " + format.format(bytes) + " bytes, " + format.format(bytes/1024d/1024d) + "MB.");
		OutputStreamWriter writer = null;
		try {
			String fname = args.length > 0 ? args[0] : "test.out";
			//writer = new FileWriter(new File(fname));
			writer = new OutputStreamWriter(new FileOutputStream(fname),Charset.defaultCharset());
			long s = System.nanoTime();
			for (int i = 0; i < LINES;i++) {
				writer.write(phrase);
			}
			writer.flush();
			long e = System.nanoTime();
			double d= (e-s)/1000000d;
			double p = (1000/d)*bytes; //bytes per sec
			double k = p/1024d; //k per sec
			double m = k/1024d; // mb per sec
			double mps = (1000/d) * LINES;
			System.out.println("Write rate: " + format.format(k) + " kb/ps, " + format.format(m) + " mb/ps. Test took " + d  + " ms. Msgs per sec: " + format.format(mps) + ".");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (Exception t) {
				t.printStackTrace();
			}
		}


	}

}
