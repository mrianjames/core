package com.oaktree.core.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Text utilities
 * 
 * @author ij
 * 
 */
public class Text {

	public static final char ZERO = '0';

	private static long today;
    public static final char STAR = '*';

    static {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTime().getTime();

	}

    /**
     * Get the millis time for midnight today. Use for getting duration in
     * millis
     *
     * @return millis for 0 o'clock (midnight).
     */
    public static long calculateToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

	/**
	 * Get the millis time for midnight today. Use for getting duration in
	 * millis
	 * 
	 * @return millis for 0 o'clock (midnight).
	 */
	public static long getToday() {
		return today;
	}

	/**
	 * Is a string null or empty?
	 * 
	 * @param engineName
	 * @return true if value is null or 0 length.
	 */
	public static boolean isBlank(String value) {
		return value == null || value.length() == 0;
	}

	/**
	 * Has a string a value?
	 * 
	 * @param name
	 * @return true if value is not null or 0 length.
	 */
	public static boolean isNotBlank(String value) {
		return value != null && value.length() > 0;
	}

	/**
	 * Given a long derive a structure that breaks this down into meaningful
	 * time units.
	 * 
	 * @author ij
	 * 
	 */
	public static class Time {
		public long hours;
		public long mins;
		public long secs;
		public long millis;

		public Time(long time) {
			hours = time / MILLIS_PER_HOUR;
            time -= hours * MILLIS_PER_HOUR;
			mins = time / MILLIS_PER_MIN;
			time -= mins * MILLIS_PER_MIN;
			secs = time / MILLIS_PER_SEC;
			time -= secs * MILLIS_PER_SEC;
			millis = time;

            if (hours >= 24) {
                hours -= 24;
            }

        }
	};

    public final static long MILLIS_PER_HOUR = 3600000;
    public final static long MILLIS_PER_MIN = 60000;
    public final static long MILLIS_PER_SEC = 1000;

	/**
	 * A datetime representation using thread local calendar.
	 * 
	 * @author ij
	 * 
	 */
	public static class DateTime {
		public Time time;
		public long day;
		public long month;
		public long year;
		private ThreadLocal<Calendar> tl = new ThreadLocal<Calendar>() {
			protected Calendar initialValue() {
				return Calendar.getInstance();
			}
		};
		private long millisSinceEpoch;

		public DateTime(long datetime) {
			Calendar c = tl.get();
			c.setTimeInMillis(datetime);
			time = new Time(datetime - today);
			day = c.get(Calendar.DATE);
			month = c.get(Calendar.MONTH) + 1;
			year = c.get(Calendar.YEAR);
			millisSinceEpoch = datetime;
		}

		public long getMillisSinceEpoch() {
			return millisSinceEpoch;
		}
	}

	/**
	 * Convert bytes to megabytes
	 * 
	 * @param bytes
	 * @return formatted rep of bytes in MB.
	 */
	public static String bytesToMB(long bytes) {
		double k = bytes / 1024d;
		double m = k / 1024d;
		return DecimalFormat.getNumberInstance().format(m) + " MB";
	}

	/**
	 * render a time portion of a full epoch time (System.currentmillis)
	 * 
	 * @param millis
	 * @return string rep of sys millis.
	 */
	public static String toTimeSinceEpoch(final long millis) {
		long m = millis - Text.getToday();
		return toTime(m);
	}

	/**
	 * Take a duration in millis since midnight and make a nice string out of
	 * it.
	 * 
	 * @param millis
	 * @return
	 */
	public static String toTime(final long millis) {
		final StringBuilder buffer = new StringBuilder();
		final Time t = new Time(millis);
		if (t.hours / 10 == 0) {
			buffer.append(Text.ZERO);
		}
		buffer.append(t.hours);
		buffer.append(Text.COLON);
		if (t.mins / 10 == 0) {
			buffer.append(Text.ZERO);
		}
		buffer.append(t.mins);
		buffer.append(Text.COLON);
		if (t.secs / 10 == 0) {
			buffer.append(Text.ZERO);
		}
		buffer.append(t.secs);
		buffer.append(Text.PERIOD);
		if (t.millis / 100 == 0) {
			if (t.millis / 10 == 0) {
				buffer.append(Text.ZERO);
			}
			buffer.append(Text.ZERO);
		}
		buffer.append(t.millis);

		return buffer.toString();
	}

	public final static String SPACE = " ";
	public static final String BRACES = "()";
	public static final String NOTHING = "";
    public static final String NOTSET = "NOT_SET";
	public static final String SEPERATOR = "|";
	public static final String DASH = "-";
	public static final String INCOMING = "INCOMING ";
	public static final String OUTGOING = "OUTGOING ";
	public static final String NOS = "NOS";
	public static final String OCRR = "OCRR";
	public static final String OCR = "OCR";
	public final static String UNKNOWN = "UNKNOWN";
	public final static String BUY = "BUY";
	public final static String SELL = "SELL";
	public final static String COLON = ":";

	/**
	 * A date formatter per thread.
	 */
	private static ThreadLocal<DateFormat> tfLocal = new ThreadLocal<DateFormat>() {
		protected DateFormat initialValue() {
			return new SimpleDateFormat("HH:mm:ss.SS");
		}
	};

	/**
	 * date formatter; thread local for thread safety.
	 */
    private static ThreadLocal<DateFormat> dtfLocal = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            return new SimpleDateFormat("E dd MMM,HH:mm:ss.SS");
        }
    };


    /**
	 * 2dp number formatter; thread local for thread safety.
	 */
    private static ThreadLocal<DecimalFormat> tf2DpLocal = new ThreadLocal<DecimalFormat>() {
		protected DecimalFormat initialValue() {
			return new DecimalFormat("#,###.##");
		}
	};
	/**
	 * 4dp number formatter; thread local for thread safety.
	 */
	private static ThreadLocal<DecimalFormat> tf4DpLocal = new ThreadLocal<DecimalFormat>() {
		protected DecimalFormat initialValue() {
			return new DecimalFormat("#,###.####");
		}
	};

	/**
	 * Render a number to 2 dp.
	 * @param number
	 * @return formatted number
	 */
	public final static String to2Dp(double number) {
		return tf2DpLocal.get().format(number);
	}

	/**
	 * Render a number to 4 dp.
	 * @param number
	 * @return formatted number
	 */
	public final static String to4Dp(double number) {
		return tf4DpLocal.get().format(number);
	}

	/**
	 * Render a nice time using threadsafe formatter.
	 * 
	 * @param start
	 * @return string represntation of a time
	 */
	public static String renderTime(long time) {
		Date dt = new Date(time);
		return tfLocal.get().format(dt);
	}
	/**
	 * Render datetime long value to a String.
	 * @param start
	 * @return string representation inc. date of time
	 */
    public static String renderDateTime(long datetime) {
        Date dt = new Date(datetime);
        return dtfLocal.get().format(dt);
    }
    /**
     * Render a Date object to formatted string.
     * @param dt
     * @return string rep of date object.
     */
	public static String renderTime(Date dt) {
		return Text.renderTime(dt.getTime());
	}
	/**
	 * Render a number to two chars. Will prefix 0 if < 10.
	 * @param number
	 * @return formatted number
	 */
	public static String twoDigits(long number) {
		if (number < 10) {
			return new StringBuffer("0").append(number).toString();
		}
		return new StringBuffer().append(number).toString();
	}

	/**
	 * Render a number to three chars. Will prefix with 0 if required.
	 * @param number
	 * @return formatted number
	 */
	public static String threeDigits(long number) {
		StringBuffer buffer = new StringBuffer();
		if (number < 100) {
			buffer.append("0");
		}
		if (number < 10) {
			buffer.append("0");
		}
		buffer.append(number);
		return buffer.toString();
	}

	/**
	 * parse 09:00:00 into millis since midnight.
	 * 
	 * @param time
	 * @return
	 */
	public static long parseTime(String time) {
		String[] parts = time.split(":");
		int hours = Integer.valueOf(parts[0]);
		int mins = Integer.valueOf(parts[1]);
		int secs = Integer.valueOf(parts[2]);
		return (hours * 3600000) + (mins * 60000) + (secs * 1000);
	}

	/**
	 * parse 09:00:00.000
	 * 
	 * @param time
	 * @return
	 */
	public static long parseTimeLong(String time) {
		String[] parts = time.split(":");
		int hours = Integer.valueOf(parts[0]);
		int mins = Integer.valueOf(parts[1]);
		int secs = Integer.valueOf(parts[2].split("[.]")[0]);
		long t = (hours * 3600000) + (mins * 60000) + (secs * 1000);

		parts = time.split("[.]");
		int millis = Integer.valueOf(parts[1]);
		return t + millis;
	}

	/**
	 * Constant for the message type for NOS
	 */
	public final static String NEW = "D";

	/**
	 * Constant for the message type for an amend.
	 */
	public final static String AMEND = "G";

	/**
	 * Constant for the message type for a cancel.
	 */
	public final static String CANCEL = "F";

	public static final char TAB = '\t';
	public static final char NEW_LINE = '\n';

	public static final String EMPTY_BOOK_STRING = "Book Empty";

	public static final String AT = "@";

	public static final String COMMA = ",";

	public static final char UNKNOWN_CHAR = 'x';

	public static final String PERIOD = ".";

	public static final String REG_PERIOD = "[.]";

	public static final String START = "(";
	public static final String END = ")";

	public static final String UNDERSCORE = "_";

	public static final String EQUALS = "=";

	public static final String LEFT_BRACKET = "(";

	public static final String RIGHT_BRACKET = ")";

	public static final String EMPTY_STRING = "";

    public static final char LEFT_SQUARE_BRACKET = '[';
    public static final char RIGHT_SQUARE_BRACKET = ']';

	/**
	 * Null checking string equality
	 * 
	 * @param str1
	 * @param str2
	 * @return true if str1 is equals to str2. otherwise false.
	 */
	public static boolean isEqual(String str1, String str2) {
		if (str1 == null) {
			if ( str2 == null) {
				return true;	
			} else {
				return false;
			}
		} else {
			if (str2 == null) {
				return false;
			} else {
				return str1.equals(str2);		
			}
		}		
	}

	/**
	 * From a properties set, extract those properties that start with the string
	 * @param properties
	 * @param exp
	 * @return properties that start with a string.
	 */
	public static Properties getProperties(Properties properties, String exp) {
		Properties newp = new Properties();
		Enumeration<Object> objects = properties.keys();
		while (objects.hasMoreElements()) {
			Object key = objects.nextElement();
			if (key instanceof String && ((String) key).startsWith(exp)) {
				newp.put(key, properties.get(key));
			}
		}
		return newp;
	}

}
