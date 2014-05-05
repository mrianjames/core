package com.oaktree.core.container;

import com.oaktree.core.threading.policy.IThreadingPolicy;
import com.oaktree.core.utils.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of a component.
 * 
 * @author Oak Tree Designs Ltd
 * 
 */
public abstract class AbstractComponent implements IComponent {

	/**
	 * A flag that denotes if this component was constructed dynamically by a
	 * container.
	 */
	private boolean dynamic = false;
	/**
	 * Our logger.
	 */
	protected final static Logger logger = LoggerFactory.getLogger(AbstractComponent.class.getName());
	/**
	 * Primary priority.
	 */
	public final static int PRIMARY = 0;
	/**
	 * Backup priority;
	 */
	public final static int SECONDARY = 1;
	/**
	 * Ranking priority. Defaults to 0, the implicit primary.
	 */
	private volatile int priority = 0;
    /**
     * Where we hand off to
     */
	protected IMessageListener messageListener;
	/**
	 * a collection of listeners; thread safe
	 */
	private List<IComponentListener> listeners = new CopyOnWriteArrayList<IComponentListener>();
	/**
	 * The type of this component.
	 */
	private ComponentType type;
	/**
	 * A components sub-type
	 */
	private String subType;
	/**
	 * Current state of this component. Starts at CREATED.
	 */
	private ComponentState state = ComponentState.CREATED;
	/**
	 * previous state; may be null
	 */
	private ComponentState previousState;
	/**
	 * Name of this component. Should be unique in the system.
	 */
	private String name;
	/**
	 * State flag for this component in paused state.
	 */
	private volatile boolean paused = false;
	/**
	 * max startup time for a component. 0 if not important.
	 */
	private long maxTimeout = 0;
	
	/**
	 * A collection of possible alternative sub types this component may be known as.
	 * TODO Change to ConcurrentSkipListSet ?
	 */
	private Set<String> alternativeSubTypes = new HashSet<String>();
	
	/**
	 * A defined policy for how this component would like to be treated regards to 
	 * threading. 
	 */
	private IThreadingPolicy threadingPolicy;

	@Override
	public void addComponentListener(IComponentListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Listener cannot be null");
		}

		listeners.add(listener);
	}

	@Override
	public String getComponentSubType() {
		return this.subType;
	}

	@Override
	public ComponentType getComponentType() {
		return this.type;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getPriority() {
		return this.priority;
	}

	@Override
	public long getStartupTimeout() {
		return this.maxTimeout;
	}

	@Override
	public synchronized ComponentState getState() {
		return this.state;
	}

	@Override
	public void initialise() {
		this.setState(ComponentState.INITIALISING);
		this.setState(ComponentState.INITIALISED);
	}

	@Override
	public boolean isDynamic() {
		return this.dynamic;
	}

	@Override
	public boolean isPaused() {
		return this.paused;
	}

	/**
	 * notify listeners of state
	 */
	protected void notifyListeners(String reason) {
		for (IComponentListener listener : listeners) {
			listener.onComponentStateChange(this, this.previousState, this.state, reason);
		}
	}

	@Override
	public void onComponentStateChange(IComponent component, ComponentState oldState, ComponentState newState,
			String changeReason) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMessage(IMessage message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMessageDeliveryFailure(IMessage message, MessageDeliveryFailure failure, String reason) {
		logger.warn("Failure to deliver message " + message.getMessageId() + " to " + message.getTargetComponentType()
				+ "." + message.getTargetSubtype() + ". Reason: " + failure.name() + ". " + reason);
	}

	@Override
	public void onUnexpectedException(IMessage message, Exception e) {
	}

	@Override
	public void pause() {
		this.paused = true;
	}

	@Override
	public void removeComponentListener(IComponentListener listener) {
		if (listener == null) {
			return;
		}
		this.listeners.remove(listener);
	}

	@Override
	public void resume() {
		this.paused = false;
	}

	@Override
	public void setComponentType(ComponentType type, String subtype) {
		this.type = type;
		this.subType = subtype;
	}

	@Override
	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

	/**
	 * Set the entity we will send messages to.
	 * 
	 * @param listener
	 */
	public void setMessageListener(IMessageListener listener) {
		this.messageListener = listener;
	}

	@Override
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Cannot set component name to null");
		}

		this.name = name;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public void setStartupTimeout(long maxmillis) {
		this.maxTimeout = maxmillis;
	}

	@Override
	public synchronized void setState(ComponentState state) {
		if (state == null) {
			throw new IllegalArgumentException("Cannot set component state to null");
		}
		// if we are already available then ignore initialise or start attempts.
		if (this.state.isAvailable() && state.isPreAvailable()) {
			return;
		}
		this.previousState = this.state;
		this.state = state;
		if (!this.state.equals(this.previousState)) {
			this.notifyListeners("State change");
		}

	}

	@Override
	public void start() {
		this.setState(ComponentState.STARTING);
		this.setState(ComponentState.STARTED);
	}

	@Override
	public void stop() {
		this.setState(ComponentState.STOPPING);
		this.setState(ComponentState.STOPPED);
	}

	@Override
	public boolean equals(Object a) {
		if (a instanceof AbstractComponent) {
			String itsname = ((AbstractComponent) a).getName();
			if (this.getName() == null) {
				if (itsname == null) {
					return true;
				} else {
					return false;
				}
			}

			return this.getName().equals(((AbstractComponent) a).getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	@Override
	public String toString() {
		return (this.getComponentType() != null ? this.getComponentType().name() : "") + Text.COLON
				+ (this.getComponentSubType() != null ? this.getComponentSubType() : "") + Text.COLON
				+ (this.getName() != null ? this.getName() : "");
	}

	@Override
	public void setComponentType(ComponentType type) {
		this.type = type;
	}

	@Override
	public void setComponentSubType(String subtype) {
		this.subType = subtype;
	}
	
	@Override
	public void setAlternativeSubTypes(String[] alt) {
		for (String type:alt) {
			this.alternativeSubTypes.add(type);
		}
	}
	
	@Override
	public String[] getAlternativeSubTypes() {
		return this.alternativeSubTypes.toArray(new String[alternativeSubTypes.size()]);
	}
	
	@Override
	public void restart() {}
	

	@Override
	public void setThreadingPolicy(IThreadingPolicy policy) {
		this.threadingPolicy = policy;
	}

	@Override
	public IThreadingPolicy getThreadingPolicy() {
		return this.threadingPolicy;
	}
}
