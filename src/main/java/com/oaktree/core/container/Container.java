package com.oaktree.core.container;

import com.oaktree.core.logging.Log;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.threading.policy.IThreadingPolicy;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference implementation of a container providing a fully reusable and generic framework for managing and running
 * custom components and tasks. This class has is essentially comprised of two things:
 * 1) An IComponentManager - a well known component that manages other components and can ensure 
 * 		correct creation of dynamic components.
 * 2) A method that provides the routing logic for messages between components.
 * 
 * The container can be considered a router of messages between either well known components, or to
 * categorised targets that require a policy based resolution (e.g. round robin). In both cases, 
 * messages routed via the container will also benefit from value added services like dispatching on 
 * an optional dispatcher (which may or may not allow prioritisation of messages) as well as the messages
 * being shown to other optional listeners (persistence, loggers, state models).
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class Container extends AbstractComponent implements IContainer {

    private ITime time = new JavaTime();

    public void setTime(ITime time) {
        this.time = time;
    }

    IDynamicComponent[] NO_TASKS = new IDynamicComponent[]{};

    /**
     * An optional dispatcher for publishing events to components; if no dispatcher is specified then we
     * publish on the senders thread.
     */
    private IDispatcher dispatcher;

    /**
     * What time this engine started up.
     */
    private long startTime = 0;


    /**
     * A list of extra message processors we show each message to.
     */
    private List<IComponent> messageProcessingComponents = new ArrayList<IComponent>();

    @Override
    public void setMessageProcessingComponents(List<IComponent> list) {
        this.messageProcessingComponents = list;
    }

    @Override
    public void addMessageProcessor(IComponent component) {
        if (component == null) {
            logger.warn("MessageProcessor was null; not adding");
        }
        this.messageProcessingComponents.add(component);
    }

    @Override
    public IComponent getComponent(String name) {
        return this.componentManager.getComponentByName(name);
    }
    /**
     * Manager of components. This object will also be able to dynamically make new objects if
     * instructed by a message.
     */
    private IComponentManager componentManager = new ComponentManager();
    /**
     * An optimised background logger.
     */
    public final static Logger logger = LoggerFactory.getLogger(Container.class.getName());
    /**
     * An optional filter of components when messages need to be passed to them.
     * This allows policies of delegation to possible components. For example,
     * round robin, random etc. If no filter is supplied then we will pick the
     * first in the list of supplied components.
     *
     * TODO filters may apply on a component type basis rather than this universal filtering.
     */
    private IComponentFilter componentFilter;

    @Override
    public IComponentManager getComponentManager() {
        return this.componentManager;
    }

    @Override
    public void registerAsComponentListener(String target, IComponentListener listener) {
        IComponent component = this.componentManager.getComponentByName(target);
        if (component != null) {
            component.addComponentListener(listener);
        }
    }

    @Override
    public void setComponentManager(IComponentManager manager) {
        this.componentManager = manager;
    }

    @Override
    public void onComponentStateChange(IComponent component, ComponentState oldState, ComponentState newState, String changeReason) {
        if (logger.isInfoEnabled()) {
            logger.info("Component " + component.getName() + " is " + newState + " because: " + changeReason + ". It was " + oldState);
        }
    }

    @Override
    public void initialise() {
        if (logger.isInfoEnabled()) {
            logger.info("Initialising " + this.getName());
        }
        this.setState(ComponentState.INITIALISING);
        if (this.componentManager == null) {
            throw new IllegalStateException("A component manager is mandatory");
        }
        this.componentManager.initialise();
        this.setState(ComponentState.INITIALISED);
        if (logger.isInfoEnabled()) {
            logger.info("Initialised " + this.getName());
        }

    }

    @Override
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("Starting " + this.getName());
        }
        this.setState(ComponentState.STARTING);
        if (this.componentManager == null) {
            throw new IllegalStateException("A component manager is mandatory");
        }
        this.componentManager.start();
        this.componentManager.addComponent(this); //add ourselves so we can be looked up or routed to.
        this.setState(ComponentState.STARTED);
        this.startTime = time.getTimeOfDay();
        if (logger.isInfoEnabled()) {
            logger.info("Started " + this.getName());
        }
    }

    @Override
    public void stop() {
        if (logger.isInfoEnabled()) {
            logger.info("Stopping " + this.getName());
        }

        this.setState(ComponentState.STOPPING);
        if (this.componentManager == null) {
            throw new IllegalStateException("A component manager is mandatory");
        }
        this.componentManager.stop();
        this.setState(ComponentState.STOPPED);
        if (logger.isInfoEnabled()) {
            logger.info("Stopped " + this.getName());
        }

    }

    @Override
    public void onMessage(final IMessage message) {

        if (logger.isTraceEnabled()) {
            logger.trace(Container.this.getName() + " has received message: " + message.toString());
        }

        try {

            //validate message.
            if (message.getTargetComponentType() == null) {
            	String rsn = "No target component type to route message with";
            	logger.warn(rsn);
            	IMessageSender sender = message.getMessageSender();
            	if (sender != null) {
            		sender.onMessageDeliveryFailure(message, MessageDeliveryFailure.MALFORMED_MESSAGE,rsn);
            	}
            	return;
            }
            
            /*
             * The only special message target is the "create a component dynamically via the manager".
             * 
             */
            if (message.getTargetComponentType().equals(ComponentType.DYNAMIC_COMPONENT_MANAGER)) {
            	Container.this.routeToDynamicComponent(message);
            	return;
            }
           
            IComponent chosen = Container.this.resolveAndChooseComponent(message);
            if (chosen == null) {
            	return;
            }

            /*
             * show message to third parties
             */
            for (IComponent processor : Container.this.messageProcessingComponents) {
                processor.onMessage(message);
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Sending message to component " + chosen.getName());
            }

            //send
            routeMessageToComponent(message,chosen);
            
        } catch (Throwable e) {
            Log.exception(logger, e);
            if (message.getMessageSender() != null) {
                message.getMessageSender().onMessageDeliveryFailure(message, MessageDeliveryFailure.UNEXPECTED_EXCEPTION,e.getMessage());
            } else {
                logger.warn("Poorly formed message: no message sender to report failure back to: " + message.getMessageId());
            }
            return;
        }

    }

    /**
     * Route a message to a component.
     * @param message
     * @param chosen
     */
    private void routeMessageToComponent(final IMessage message, final IComponent chosen) {
        try {
        	IThreadingPolicy policy = chosen.getThreadingPolicy();
        	String dispatchid = policy != null ? policy.getDispatchKey(message) : null;
        	if (dispatchid != null && dispatcher != null) {
        		Container.this.dispatcher.dispatch(dispatchid, new Runnable() {
        			public void run() {
        				try {
        					chosen.onMessage(message);
        				} catch (Throwable t) {
        					if (message.getMessageSender() != null) {
                                message.getMessageSender().onMessageDeliveryFailure(message, MessageDeliveryFailure.UNEXPECTED_EXCEPTION,t.getMessage());
                            } else {
                                logger.warn("Cannot return message exception as no sender...");
                                Log.exception(logger, t);
                            }
                            return;		
        				}
        			}
        		});                    		
        	} else {
        		//on this thread.
        		try {
        			chosen.onMessage(message);
        		} catch (Throwable t) {
        			Log.exception(logger, t);
					if (message.getMessageSender() != null) {
                        message.getMessageSender().onMessageDeliveryFailure(message, MessageDeliveryFailure.UNEXPECTED_EXCEPTION,t.getMessage());
                    } else {
                        logger.warn("Cannot return message exception as no sender...");                        
                    }
                    return;		
				}
        	}
        } catch (Exception e) {
            chosen.onUnexpectedException(message, e);
            if (message.getMessageSender() != null) {
                message.getMessageSender().onMessageDeliveryFailure(message, MessageDeliveryFailure.UNEXPECTED_EXCEPTION,e.getMessage());
            } else {
                logger.warn("Cannot return message exception as no sender...");
                Log.exception(logger, e);
            }
            return;
        }

	}

	/*
     * resolve a collection of components we could possibly use; then filter that down
     * with our possible policy to give us one definite target.
     * Note that there is a window of opportunity for a component to go unavailable
     * after we have chosen it depending on the threading of availability setting
     * and the threading of this dispatch; components should check their own current
     * availability in this instance.
     * If no filter is available or size is only 1 then we proceed with that 1 returned.
     */
    protected IComponent resolveAndChooseComponent(final IMessage message) {    	
        List<IComponent> possibles = message.isSendToUnavailableTarget() ? Container.this.componentManager.getComponentsByCategory(message.getTargetComponentType(), message.getTargetSubtype()): Container.this.componentManager.getAvailableComponentsByCategory(message.getTargetComponentType(), message.getTargetSubtype());
        if (possibles.isEmpty()) {
            logger.warn("No possible components found for message: " + message.getDescription());
            if (message.getMessageSender() != null) {
                message.getMessageSender().onMessageDeliveryFailure(message, MessageDeliveryFailure.NO_COMPONENT_FOUND,message.getTargetComponentType().name() +":" + message.getTargetSubtype());
            } else {
                logger.warn("Message cannot be responded to as it was from an anonymous source");
            }
            return null;
        }
        IComponent chosen = null;
        if (possibles.size() > 1 && Container.this.componentFilter != null) {
            chosen = Container.this.componentFilter.filter(possibles);
        } else {
            chosen = possibles.get(0);
        }
        return chosen;
	}

	/**
     * Route messages to a dynamic component for dynamic creation/resolution.
     * 
     * @param message
     */
    private void routeToDynamicComponent(final IMessage message) {
        IDynamicComponent[] tasks = NO_TASKS;
        try {
            tasks = Container.this.componentManager.resolveDynamicComponent(message);
        } catch (Throwable t) {
            Log.exception(logger, t);
            if (message.getMessageSender() != null) {
            	message.getMessageSender().onMessageDeliveryFailure(message, MessageDeliveryFailure.NO_COMPONENT_COULD_BE_MANUFACTURED,message.getTargetComponentType().name() +":" + message.getTargetSubtype());
            }
            return;
        }
        /*
         * its now pertinent to look for others that are interested in processing these messages, for example
         * persistence, state models, story publication.
         */
        if (tasks != null && tasks.length > 0) {

            for (IComponent processor : Container.this.messageProcessingComponents) {
                processor.onMessage(message);
            }
        } else {
        	logger.warn("No component could be manufactured for message: " + message);
        	if (message.getMessageSender() != null) {
        		message.getMessageSender().onMessageDeliveryFailure(message, MessageDeliveryFailure.NO_COMPONENT_COULD_BE_MANUFACTURED,message.getTargetComponentType().name() +":" + message.getTargetSubtype());
        	}
            return;
        }

        for (IDynamicComponent task : tasks) {
            if (task != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Sending message to task " + task.getName());
                }
                task.addComponentListener(Container.this);
                this.routeMessageToComponent(message, task);
                return;
            }
        }

	}

	@Override
    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void setComponentFilter(IComponentFilter componentFilter) {
        this.componentFilter = componentFilter;
    }

    @Override
    public void onUnexpectedException(IMessage message, Exception e) {
        Log.exception(logger, e);
    }

    @Override
    public IDispatcher getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public long getStartupTime() {
        return this.startTime;
    }

}
