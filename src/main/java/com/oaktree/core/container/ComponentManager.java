package com.oaktree.core.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.utils.Text;

/**
 * Manager of components. Responsible for starting/stopping/vending of components.
 * 
 * Note that initialisation of components added to this collection will only occur if not already initialised
 * Also, starting will only occur on start if we are not already in start state. This is done because we
 * do not want components already initialised/started repeat the excercise.
 * 
 * @author Oak Tree Designs Ltd
 *
 */
public class ComponentManager extends AbstractComponent implements IComponentManager {

    /**
     * dynamic component maker. this could be a resolver of factories.
     */
    private IDynamicComponentFactory dcFactory;
    /**
     * Possible listener to lifecycle events on the component manager e.g. adding component.
     */
    private IComponentManagerListener listener;
    private boolean wait;
    private List<ComponentType> ordering;
    /**
     * a map of maps keyed by component type, then sub type of lists of components.
     */
    private ConcurrentHashMap<ComponentType, ConcurrentHashMap<String, List<IComponent>>> componentsByType = new ConcurrentHashMap<ComponentType, ConcurrentHashMap<String, List<IComponent>>>();
    /**
     * a map of all components keyed on the component name.
     */
    private Map<String, IComponent> componentsByName = new ConcurrentHashMap<String, IComponent>();
    private static Logger logger = LoggerFactory.getLogger(ComponentManager.class.getName());

    @Override
    public void addComponent(final IComponent component) {
        if (component.getName() == null) {
            throw new IllegalStateException("A component MUST have a name: " + component.getClass().getName());
        }
        if (component.getComponentType() == null) {
            throw new IllegalStateException("A component MUST have a type: " + component.getName());
        }
        ComponentType type = component.getComponentType();
        if (this.componentsByName.containsKey(component.getName())) {
            throw new IllegalArgumentException("Component with the name " + component.getName() + " is already registered");
        }
        this.componentsByName.put(component.getName(), component);
        
        ConcurrentHashMap<String, List<IComponent>> map = this.componentsByType.get(type);
        if (map == null) {
            map = new ConcurrentHashMap<String, List<IComponent>>();
            ConcurrentHashMap<String, List<IComponent>> m = this.componentsByType.putIfAbsent(component.getComponentType(), map);
            if (m != null) {
                map = m;
            }
        }
        String key = component.getComponentSubType();
        if (key == null) {
            key = Text.NOTHING;
        }
        List<IComponent> list = map.get(key);
        if (list == null) {
            /*
             * TODO not threadsafe. use concurrenthashmap?
             */

            list = new ArrayList<IComponent>();
            map.put(key, list);
        }
        list.add(component);
        /*
         * apply any alternative sub types in the same way.
         */
        for (String alias:component.getAlternativeSubTypes()) {
        	list = map.get(alias);
            if (list == null) {
                /*
                 * TODO not threadsafe. use concurrenthashmap?
                 */

                list = new ArrayList<IComponent>();
                map.put(alias, list);
            }
            list.add(component);
        }

        if (this.listener != null) {
            this.listener.onComponentAdded(component);
        }
    }

    @Override
    public IComponent getComponentByName(String componentName) {
        return this.componentsByName.get(componentName);
    }

    @Override
    public List<IComponent> getComponentsByCategory(ComponentType category, String subtype) {
        List<IComponent> l = new ArrayList<IComponent>();
        if (subtype == null) {
            subtype = Text.NOTHING;
        }
        if (category.equals(ComponentType.NAMED)) {
            IComponent c = this.getComponentByName(subtype);
            if (c != null) {
            	l.add(c);
            }
            return l;
        } else {
            Map<String, List<IComponent>> map = this.componentsByType.get(category);
            if (map == null) {
                return l;
            }
            return map.get(subtype);
        }
    }

    @Override
    public IComponent getComponentByCategory(ComponentType categor, String subtype) {
        List<IComponent> possibles = this.getComponentsByCategory(categor, subtype);
        if (possibles != null && possibles.size() >= 1) {
            return possibles.get(0);
        }
        return null;
    }

    @Override
    public void removeComponent(IComponent component, boolean stop) {
        if (component == null) {
            throw new IllegalArgumentException("Null component supplied");
        }
        if (stop) {
            component.stop();
        }
        this.componentsByName.remove(component.getName());
        if (component.getComponentType() != null) {
            Map<String, List<IComponent>> m = this.componentsByType.get(component.getComponentType());
            if (m != null) {
                String key = component.getComponentSubType();
                if (key == null) {
                    key = Text.NOTHING;
                }
                m.remove(key);
            }
        }

        if (this.listener != null) {
            this.listener.onComponentRemoved(component);
        }
    }

    @Override
    public void setStartupOrdering(List<ComponentType> ordering) {
        this.ordering = ordering;
    }

    @Override
    public void setWaitBetweenStartupTypes(boolean wait) {
        this.wait = wait;
    }

    @Override
    public void onComponentStateChange(IComponent component,
            ComponentState oldState, ComponentState newState,
            String changeReason) {
    }

    @Override
    public void initialise() {
        this.setState(ComponentState.INITIALISING);
        if (this.ordering != null) {
            for (ComponentType type : this.ordering) {
                Map<String, List<IComponent>> map = this.componentsByType.get(type);
                if (map != null) {
                    for (List<IComponent> components : map.values()) {
                        for (IComponent c : components) {
                            if (!c.getState().haveInitialised()) {
                            	logger.info("Initialising component " + c.getName());
                                c.initialise();
                                logger.info("Initialised component " + c.getName());
                            }
                        }
                    }
                }
            }
        } else {
            for (IComponent component : this.componentsByName.values()) {
                if (!component.getState().haveInitialised()) {
                	logger.info("Initialising component " + component.getName());
                    component.initialise();
                    logger.info("Initialised component " + component.getName());
                }
            }
        }
        this.setState(ComponentState.INITIALISED);
    }

    @Override
    public void start() {
        this.setState(ComponentState.STARTING);
        if (this.ordering != null) {
            for (ComponentType type : this.ordering) {
                boolean alldone = false;
                List<IComponent> all = new ArrayList<IComponent>();
                Map<String, List<IComponent>> map = this.componentsByType.get(type);
                if (map != null) {
                    for (List<IComponent> components : map.values()) {
                        all.addAll(components);
                        for (IComponent c : components) {
                            if (logger.isInfoEnabled()) {
                                logger.info("Starting component " + c.getName());
                            }
                            c.start();
                            if (logger.isInfoEnabled()) {
                                logger.info("Started component " + c.getName());
                            }      
                        }
                    }
                } else {
                    logger.warn("You have no components of type " + type + " to start");
                }
                if (this.wait) {
                    /*
                     * wait.
                     */
                    while (!alldone) {
                        int done = 0;
                        for (IComponent c : all) {
                            if (c.getState().isStarted()) {
                                done++;
                            }
                        }
                        if (done == all.size()) {
                            alldone = true;
                        } else {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            }
        } else {
            for (IComponent component : this.componentsByName.values()) {
                if (!component.getState().isStarted()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Starting component " + component.getName());
                    }
                    component.start();
                    if (logger.isInfoEnabled()) {
                        logger.info("Started component " + component.getName());
                    }
                }
            }
        }
        this.setState(ComponentState.STARTED);
    }

    @Override
    public void stop() {
        this.setState(ComponentState.STOPPING);
        /*
         * stop in reverse "ordering".
         */
        if (this.ordering != null) {
            for (ComponentType type : this.ordering) {
                Map<String, List<IComponent>> map = this.componentsByType.get(type);
                if (map != null) {
	                for (List<IComponent> components : map.values()) {
	                	if (components != null) {
		                    for (IComponent c : components) {
		                    	if (c != null) {
		                    		c.stop();
		                    	}
		                    }
	                	}
	                }
                }
            }
        } else {
            for (IComponent component : this.componentsByName.values()) {
                component.stop();
            }
        }
        this.setState(ComponentState.STOPPED);
    }

    @Override
    public List<IComponent> getAvailableComponentsByCategory(
            ComponentType category, String subtype) {
        List<IComponent> possibles = this.getComponentsByCategory(category, subtype);
        if (possibles == null) {
            return new ArrayList<IComponent>();
        }
        List<IComponent> as = new ArrayList<IComponent>();
        for (IComponent possible : possibles) {
            if (possible.getState().isAvailable()) {
                as.add(possible);
            }
        }
        return as;
    }

    @Override
    public IComponent getDynamicComponent(String name) {
        return this.getComponentByName(name);
    }

    @Override
    public void pauseTask(String task) {
        IComponent comp = this.getComponentByName(task);
        if (comp != null) {
            comp.pause();
        }
    }

    @Override
    public void removeTask(IDynamicComponent task) {
        this.componentsByName.remove(task.getName());
        if (this.listener != null) {
            this.listener.onComponentRemoved(task);
        }

    }

    @Override
    public IDynamicComponent[] resolveDynamicComponent(IMessage message) {
        if (this.dcFactory != null) {
            IDynamicComponent[] comps = this.dcFactory.resolve(message);
            for (IDynamicComponent comp : comps) {
                if (comp == null) {
                    throw new IllegalStateException("Cannot make a dynamic component");
                }
                /*
                 * do all the neccesary init and startup for a newly created component; otherwise
                 * dont bother.
                 */
                if (comp.getState().equals(ComponentState.CREATED)) {
                    comp.setDynamic(true);
                    this.addComponent(comp);
                    comp.setMessageListener(this.messageListener);
                    comp.initialise();
                    comp.start();
                }
            }
            return comps;
        }
        return new IDynamicComponent[0];
    }

    @Override
    public void resumeComponent(String task) {
        IComponent comp = this.getComponentByName(task);
        if (comp != null) {
            comp.resume();
        }
    }

    @Override
    public void startComponent(String task) {
        IComponent comp = this.getComponentByName(task);
        if (comp != null) {
            comp.start();
        }

    }

    @Override
    public void stopComponent(String task) {
        IComponent comp = this.getComponentByName(task);
        if (comp != null) {
            comp.stop();
        }

    }

    @Override
    public void setDynamicComponentFactory(IDynamicComponentFactory factory) {
        this.dcFactory = factory;
    }

    @Override
    public int getNumComponents() {
        return this.componentsByName.size();
    }

    @Override
    public void addAlias(String alias, IComponent d) {
        this.componentsByName.put(alias, d);
    }

    @Override
    public IComponent[] getComponents() {
        return this.componentsByName.values().toArray(new IComponent[this.componentsByName.values().size()]);
    }

    @Override
    public IDynamicComponent[] getDynamicComponents() {
        List<IComponent> dcs = new ArrayList<IComponent>();
        for (IComponent c : this.componentsByName.values()) {
            if (c.isDynamic()) {
                dcs.add((IDynamicComponent) c);
            }
        }
        return dcs.toArray(new IDynamicComponent[dcs.size()]);
    }

    @Override
    public String[] getDynamicComponentsAsString() {
        List<String> dcs = new ArrayList<String>();
        for (IComponent c : this.componentsByName.values()) {
            if (c.isDynamic()) {
                dcs.add(c.getName());
            }
        }
        return dcs.toArray(new String[dcs.size()]);
    }

    @Override
    public void setListener(IComponentManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeListener(IComponentManagerListener listener) {
        this.listener = null;
    }

    @Override
    public void pause() {
        super.pause();
        for (IComponent component: this.componentsByName.values()) {
            component.pause();
        }
    }

    @Override
	public void clear() {
		this.componentsByName.clear();
		this.componentsByType.clear();
	}
}
