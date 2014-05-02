package com.oaktree.core.container;

/**
 * Components have the following "states":
CREATED - default state on construction of component
INITIALISING - marks the start of initialise phase.
INITIALISED - marks the completion of the initialisation phase.
STARTING - marks the start of the start phase.
STARTED - marks the end of the start phase.
AVAILABLE - marks that a component is fully up and running. ComponentManager filtering relies on this state as an indication that a component is routable.
UNAVAILABLE - marks that a component that though probably initialised and started has become unroutable but probably not from stopping.
STOPPING - marks the start of the stopping phase.
STOPPED - marks the end of the stopped phase.
RESTARTING - marks the start of the restarting phase.
 * <img src="../../../../../../../images/state.jpg"/>
 * @author IJ
 *
 */
public enum ComponentState {
	STOPPED,INITIALISING,INITIALISED,STARTING,STARTED,AVAILABLE, STOPPING, CREATED, UNAVAILABLE;
	
	/**
	 * Is our state exactly STARTED?
	 * @return
	 */
	public boolean isStarted() {
		return this.equals(STARTED);
	}
	public boolean isAvailable() {
		return this.equals(AVAILABLE);		
	}
	public boolean isNotAvailable() {
		return !this.isAvailable();
	}
	public boolean isInitialised() {
		return this.equals(INITIALISED);
	}
	public boolean haveInitialised() {
		return this.equals(INITIALISED) || this.equals(STARTING) ||this.equals(STARTED)||this.equals(AVAILABLE);
	}
	/**
	 * Are we in STARTED or AVAILABLE state?
	 * @return
	 */
	public boolean haveStarted() {
		return this.equals(STARTED)||this.equals(AVAILABLE);
	}
	public boolean shouldInitialise() {
		return !(this.haveInitialised() || this.equals(INITIALISING));
	}
	public boolean shouldStart() {
		return !(this.haveStarted() || this.equals(STARTING));
	}
	public boolean isStopped() {
		return this.equals(STOPPED);
	}
	public boolean isStopping() {
		return this.equals(STOPPING);
	}
	public boolean isStoppingOrStopped() {
		return this.isStopped() || this.isStopping();
	}
	/**
	 * Is this state a state that comes before AVAIALBLE. So, initialising, initialised, starting, started.
	 * @return
	 */
	public boolean isPreAvailable() {
		return this.isStarted() || this.isStarting() || this.isInitialising() || this.isInitialised();
	}
	public boolean isStarting() {
		return this.equals(STARTING);
	}
	public boolean isInitialising() {
		return this.equals(INITIALISING);
	}
}
