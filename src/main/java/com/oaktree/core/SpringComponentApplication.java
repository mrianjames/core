/**
 * 
 */
package com.oaktree.core;

import com.oaktree.core.container.IBasicLifecyleComponent;
import com.oaktree.core.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic spring "application" startup; will build the context, create your main "application object"
 * and then call a start method on it.
 * 
 * Use -Drecover=true to put into recovery rather than startup mode.
 * @author jameian
 *
 */
public class SpringComponentApplication {

	/**
	 * Container LOGGER.
	 */
	private static final Logger logger = LoggerFactory.getLogger(SpringComponentApplication.class.getName());
	

	/**
	 * Go forth, and start young jedi.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if(args.length < 1){
                            logger.error("Please supply spring file(s)");
				System.exit(1);
			}
			List<String> parsedArgs = new ArrayList<String>();
			for (String arg:args) {
				if (arg.endsWith(".xml")) {
					parsedArgs.add(arg);
				}
			}
			args = parsedArgs.toArray(new String[]{});
			FileSystemXmlApplicationContext context
			= new FileSystemXmlApplicationContext(args);


            IBasicLifecyleComponent application = (IBasicLifecyleComponent)context.getBean("application");
                        application.initialise();
                        application.start();
                        
			if (logger.isInfoEnabled()) {
                            logger.info("Component " + application.getName() + " started.");
                        }

            Thread.currentThread().join();

		} catch (Throwable e) {
			Log.exception(logger,e);
			System.exit(-1);
		}
	}

}
