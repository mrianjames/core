/**
 * 
 */
package com.oaktree.core.logging;

import org.slf4j.Logger;



/**
 * Log utility print outs for exceptions etc.
 * 
 * @author Ian James
 */

public class Log {

	

	/**
	 * display a throwable in a nice fashion; includes the stack trace
	 * and will output to our chosen logger.
	 * @param logger
	 * @param e
	 */
	public static void exception(Logger logger, Throwable e) {
		try {
		if (e == null) {
			return;
		}
        StackTraceElement[] stack = e.getStackTrace();
        if (stack.length == 0) {
    		logger.error(
    			e.getClass().getName() +
    			" " +
    			e.getMessage()
    		);
    		return;
        }
        /*
         * Log the name of the exception and the message under the name of the
         * class at the bottom of the stack.
         */
		stack[stack.length - 1].getClassName();
		logger.error(e.getClass().getName() + 
				" " +
				e.getMessage());
		/*
		 * Log the stack trace.
		 */
		String trace	= "    in ";
		String line		= " at line ";
		String text		= null;
		int lineNumber	= 0;
		for (int i = 0; i < stack.length; i++) {
			text =
				trace +
				stack[i].getClassName() +
				" " +
				stack[i].getMethodName() +
				"()";
			lineNumber = stack[i].getLineNumber();
			if (lineNumber > 0) {
				logger.error(text + line + lineNumber);
			} else {
				logger.error(text);
			}
		}
		} catch (Exception ex) {
			//Logger.getAnonymousLogger().warning("Cannot handle exception properly");
			ex.printStackTrace();
		}
	}

	
	/**
	 * log an exception with your own special 
	 * @param logger
	 * @param e
	 * @param msg
	 */
	public static void exception(Logger logger,Throwable e, String msg) {
		logger.warn(msg);
		exception(logger,e);
	}

}
