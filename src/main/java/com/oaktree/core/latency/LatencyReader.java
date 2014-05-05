package com.oaktree.core.latency;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.logging.Log;

/**
 * Read and analyse engine latency performance. Displays the 50/90/99 percentile
 * of all sections. TOTAL is from the start of the start tag to the end of the
 * endTag.
 * 
 * @author ij
 * 
 */
public class LatencyReader {

	// search for just an order id.
	private String orderid;

	public LatencyReader() {
	}

	private String filename;
	private List<String> lines = new ArrayList<String>();
	private final static Logger logger = LoggerFactory.getLogger(LatencyReader.class);
	private String startTag = "UPSTREAM";
	private String endTag = "ORDER_BOOK";

	public void start() {
		FileReader reader = null;
		BufferedReader breader = null;
		try {
			reader = new FileReader(this.filename);
			breader = new BufferedReader(reader);
			String line = breader.readLine();
			while (line != null) {
				lines.add(line);
				line = breader.readLine();
			}
			logger.info("Read " + (this.lines.size() - 1)
					+ " lines of latency. ");
			if (lines.size() < 2) {
				logger.error("Not enough lines for reader.");
				throw new IllegalStateException("Not enough lines for reader");
			}
			if (this.lines.size() > 0) {
				this.lines.remove(0);
			}
			this.load();
			logger.info("Loaded");
			this.analyse();
			logger.info("Analysed");
			logger.info("AllDone");
		} catch (Exception e) {
			Log.exception(logger, e);			
		} finally {
			try {
				reader.close();

			} catch (Exception t) {
			}
			try {
				breader.close();
			} catch (Exception t) {
			}
		}

	}

	private void render(String type) {
		Map<String, DescriptiveStatistics> stats = new HashMap<String, DescriptiveStatistics>();
		DescriptiveStatistics s = new DescriptiveStatistics();
		List<Record> recs = this.recordsByType.get(type);
		for (Record record : recs) {
			if (this.orderid != null && this.orderid.length() > 0
					&& record.id.equals(orderid)
					|| (this.orderid == null || this.orderid.length() == 0)) {
				if (record.starttime > 0 && record.endtime > 0) {
					s.addValue(record.getDurationNanos());
				} else {
					logger.debug("Record " + record.id + " of type "
							+ record.type + " is malformed");
				}
			}
		}
		stats.put(type, s);
		double MILLION = 1000000;
		DecimalFormat df = new DecimalFormat("#,###.##");
		double fifty = s.getPercentile(50) / MILLION;
		double ninety = s.getPercentile(90) / MILLION;
		double ninetynine = s.getPercentile(99) / MILLION;
		logger.info(type + " 50%: " + df.format(fifty) + "ms, 90%: "
				+ df.format(ninety) + "ms, 99%: " + df.format(ninetynine)
				+ "ms");
	}

	private void analyse() {

		for (String type : this.recordsByType.keySet()) {
			if (!type.equals("TOTAL")) {
				this.render(type);
			}
		}
		logger.info("**************************************");
		this.render("TOTAL");
		logger.info("**************************************");

	}

	private void load() {
		try {
			for (String line : this.lines) {
				String[] bits = line.split(",");
				long time = Long.valueOf(bits[0]);
				String phase = bits[1];
				String type = bits[2];
				String key = bits[3];
				String subkey = bits[4];
				long id = Long.valueOf(bits[5]);
				String uniqueId = type + "|" + key + "|" + subkey + "|" + id;
				if (phase.equals(BEGIN)) {
					Record record = new Record(time, type, uniqueId);
					recordsById.put(uniqueId, record);
					List<Record> r = this.recordsByType.get(type);
					if (r == null) {
						r = new ArrayList<Record>();
						this.recordsByType.put(type, r);
					}
					r.add(record);
					if (type.equals(startTag)) {
						String i = subkey + "|" + id;
						record = new Record(time, "TOTAL", i);
						recordsById.put("TOTAL|" + i, record);
						r = this.recordsByType.get("TOTAL");
						if (r == null) {
							r = new ArrayList<Record>();
							this.recordsByType.put("TOTAL", r);
						}
						r.add(record);
					}
				} else {
					Record record = this.recordsById.get(uniqueId);
					if (record == null) {
						logger
								.error("No record found for end section: "
										+ line);
					} else {
						record.endtime = time;
					}

					if (type.equals(this.endTag)) {
						String i = subkey + "|" + id;
						Record r = this.recordsById.get("TOTAL|" + i);
						if (r != null) {
							r.endtime = time;
						}
					}

				}
				// logger.info("Processed " + c++);
			}
		} catch (Exception e) {
			Log.exception(logger, e);
		}
	}

	private String BEGIN = "BEG";

	private Map<String, Record> recordsById = new HashMap<String, Record>();
	private Map<String, List<Record>> recordsByType = new HashMap<String, List<Record>>();

	static class Record {

		public Record(long starttime, String type, String id) {
			this.starttime = starttime;
			this.type = type;
			this.id = id;
		}

		public long starttime;
		//public String phase;
		public String type;
		public String id;
		public long endtime;

		public long getDurationNanos() {
			return endtime - starttime;
		}

		public double getDurationMillis() {
			return this.getDurationNanos() / 1000000d;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 4) {
			System.err
					.println("File, BEGIN tag, END tag, removefirst, <orderid>");
			System.exit(-1);
		}

		LatencyReader reader = new LatencyReader();
		reader.filename = args[0];
		reader.startTag = args[1];
		reader.endTag = args[2];
		// reader.removefirst = Integer.valueOf(args[3]);
		if (args.length > 4) {
			reader.orderid = args[4];
		}

		reader.start();
	}
}
