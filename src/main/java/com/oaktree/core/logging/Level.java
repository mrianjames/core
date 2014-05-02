package com.oaktree.core.logging;

/**
 * Logging level. Uses log4j standards but capable of consuming java.util names.
 * 
 */
public enum Level {
    DEBUG(400),TRACE(500),INFO(800),WARN(900),ERROR(1000),OFF(Integer.MAX_VALUE),ALL(Integer.MIN_VALUE);
    private final int value;

    Level(int value) {
        this.value = value;
    }
    public int intValue() {
        return value;
    }
    
    public static Level fromInt(int value) {
    	switch (value) {
    	case 400:
    		return DEBUG;
    	case 500:
    		return TRACE;
    	case 800:
    		return INFO;
    	case 900:
    		return WARN;
    	case 1000:
    		return ERROR;
    	case Integer.MAX_VALUE:
    		return OFF;
    	case Integer.MIN_VALUE:
    		return ALL;
    	}
    	throw new IllegalStateException("Cannot parse logging level "+value);
    }

    public static Level parse(String level) {
        try {
            return Level.valueOf(level); //if it fails does it throw or null?            
        } catch (Exception e) {
            return parseLegacy(level);
        }
    }

    /**
     * Parse java util levels into our std enumeration levels.
     * @param level
     * @return
     */
    public static Level parseLegacy(String level) {
        if ("FINEST".equals(level)) {
            return DEBUG;
        } else if ("FINER".equals(level) || "FINE".equals(level)) {
            return TRACE;
        } else if ("INFO".equals(level)) {
            return INFO;
        } else if ("WARNING".equals(level)) {
            return WARN;
        }else if ("SEVERE".equals(level)) {
            return ERROR;
        }else if ("ALL".equals(level)) {
            return ALL;
        }else if ("OFF".equals(level)) {
            return OFF;
        }
        throw new IllegalStateException("Invalid level: " + level);
    }
}
