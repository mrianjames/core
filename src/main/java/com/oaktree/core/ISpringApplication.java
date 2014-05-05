package com.oaktree.core;

/**
 * A spring runnable application.
 * @author ij
 */
public interface ISpringApplication {
        /**
         * Start the spring application.
         */
	public void start();
        /**
         * Recover the spring application
         */
        public void recover();

        /**
         * Stop the spring application.s
         */
        public void stop();
}
