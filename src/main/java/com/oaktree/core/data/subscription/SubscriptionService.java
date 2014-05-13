package com.oaktree.core.data.subscription;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.collection.HashMapFactory;
import com.oaktree.core.collection.SetFactory;
import com.oaktree.core.collection.multimap.IMultiMap;
import com.oaktree.core.collection.multimap.MultiMap;
import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentType;
import com.oaktree.core.container.IComponent;
import com.oaktree.core.data.cache.DataCache;
import com.oaktree.core.data.IData;
import com.oaktree.core.data.cache.IDataCache;
import com.oaktree.core.data.IDataProvider;
import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.threading.dispatcher.IDispatcher;
import com.oaktree.core.time.ITime;
import com.oaktree.core.time.JavaTime;

/**
 * This class is an archetype service to facilitate the subscription and retrieval of
 * data. Subscribers can make several type of request:
 * 1) Async snap - you request data, get immediate async response if data exists.
 * 2) Sync snap - you request data and get immediate function return with the data or null if not existing.
 * 3) Subscribe - you request data and get any future updates on that which match your subscription.
 * 4) Snap and Subscribe (async) - you subscribe and get the last image as an initial update, followed by all future updates.
 *
 * Async callbacks will use the dispatcher to send data back on the IDataListener nameable.
 * Conflation is the process of skipping over intermediate updates - this is an optional feature.
 *
 * Created by ianjames on 06/05/2014.
 */
public class SubscriptionService<T extends IData<String>> extends AbstractComponent implements ISubscriptionService<T>,IDataReceiver<T> {
    private final static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    public SubscriptionService(String name) {
        setName(name);
        setComponentType(ComponentType.SERVICE);
        setComponentSubType("SubscriptionService");
    }

    /**
     * The cache.
     */
    private IDataCache<T,String> cache = new DataCache<T,String>();

    /**
     * For timestamping when we pass data on.
     */
    private ITime timeProvider = new JavaTime();

    /**
     * Map of subscription collections keyed on the subscription key.
     * Concurrent
     */
    private IMultiMap<String,ISubscriptionRequest<T>> requests = new MultiMap<String,ISubscriptionRequest<T>>(new HashMapFactory<String,Collection<ISubscriptionRequest<T>>>(100,true),new SetFactory<ISubscriptionRequest<T>>(10,true));

    /**
     * Providers are the sources of data. We keep a collection of these so we can notify them of what we are and are not
     * interested in. They may or may not care.
     */
    private Set<IDataProvider<T>> providers = new CopyOnWriteArraySet<IDataProvider<T>>();

    /**
     * conflation is the practice of skipping intermediate updates we receive in the time we are already pending
     * notification to a client of a previous update. This is useful where incoming rates > consumption rates
     * but can only be done where all T's for a key are full snapshots, replacing any previous version of that keyed data.
     */
    private boolean conflation = false;

    /**
     * Filters down lists of providers we need to register/unregister interests on.
     */
    private IDataProviderFilter<T> providerFilter;

    /**
     * Manages all the threading of notifications of data updates to receivers.
     */
    private IDispatcher dispatcher;
    public void setDispatcher(IDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public ISubscriptionResponse<T> subscribe(ISubscriptionRequest<T> request) {
        //can be concurrent at this stage...
        SubscriptionResponse<T> response = new SubscriptionResponse<T>();
        try {
            if (logger.isInfoEnabled()) {
                logger.info(getName() + " subscription: " + request);
            }
            if (request.getDataReceiver() == null) {
                response.fail("Invalid receiver - null");
                logger.warn("Subscription failure: "+response.getFailureReason());
                return response;
            }
            if (request.getDataReceiver().getName() == null) {
                response.fail("Invalid receiver - null name");
                logger.warn("Subscription failure: "+response.getFailureReason());
                return response;
            }
            if (request.getKey() == null) {
                response.fail("Invalid, null key requested");
                logger.warn("Subscription failure: "+response.getFailureReason());
                return response;
            }

            if (SubscriptionType.isSnap(request.getSubscriptionType())) {
                T snap = cache.snap(request.getKey());
                response.setInitial(snap);
                response.setSubscriptionResult(SubscriptionResult.SUBSCRIPTION_OK);
                if (snap != null && SubscriptionType.isAsync(request.getSubscriptionType())) {
                    request.getDataReceiver().onData(snap, this, getTime());
                }
            }
            if (SubscriptionType.isSubscribe(request.getSubscriptionType())) {
                response.setSubscriptionResult(doSubscribe(request));
                doSubscribe(request);
            }
        } catch (Throwable t) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed subscription request " + request, t);
            }
            response.setSubscriptionResult(SubscriptionResult.SUBSCRIPTION_FAILURE);
            response.setInitial(null);
            response.setFailureReason(t.getMessage());
        }

        return response;
    }

    @Override
    public ISubscriptionResponse<T> unsubscribe(ISubscriptionRequest<T> request) {
    	 SubscriptionResponse<T> response = new SubscriptionResponse<T>();
         try {
             if (logger.isInfoEnabled()) {
                 logger.info(getName() + " unsubscribe: " + request);
             }
             if (request == null) {
                 response.fail("Invalid request - null");
                 logger.warn("Unsubscribe failure: "+response.getFailureReason());
                 return response;
             }
             if (request.getDataReceiver() == null) {
                 response.fail("Invalid receiver - null");
                 logger.warn("Unsubscribe failure: "+response.getFailureReason());
                 return response;
             }
             requests.remove(request.getKey(), request);
         } catch (Throwable t) {
        	 response.fail("Unexpected unsubscribe error: "+t.getMessage());
             logger.warn("Unsubscribe failure: "+response.getFailureReason());
             return response;
         }
        return response;
    }

    /**
     * The work of subscribing to a request. Manage the subscription, passing on
     * any interest to any found provider that is deemed worthy.
     * @param request
     * @return
     */
    private SubscriptionResult doSubscribe(ISubscriptionRequest<T> request) {
        synchronized (request) { //TODO review.
            if (logger.isTraceEnabled()) {
                logger.trace("Adding request..." + request);
            }
            requests.put(request.getKey(), request);

            //resolve source of data to pass request onto...they might not be interested in our key, but
            //some will do further subscriptions to external systems on our behalf, so nice to tell them what we want.
            Collection<IDataProvider<T>> providers = resolveProviders(request.getKey());
            for (IDataProvider<T> provider:providers) {
               provider.registerInterest(request.getKey(),this);
            }
        }
        return SubscriptionResult.SUBSCRIPTION_OK;
    }

    public void setDataProviderFilter(IDataProviderFilter<T> filter) {
        this.providerFilter = filter;
    }
    /**
     * Given a key, find a provider we should notify our interest
     * @param key
     * @return
     */
    private Collection<IDataProvider<T>> resolveProviders(String key) {
        if (providerFilter == null) {
            return providers;
        } else {
            return providerFilter.filter(providers,key);
        }
    }

    public void addProvider(IDataProvider<T> provider) {
        providers.add(provider);
        //in-case. hope its a set!
        provider.addDataReceiver(this);
    }
    /**
     * Simple spring inject
     */
    public void setProvider(IDataProvider<T> provider) {
        this.addProvider(provider);
    }
    public void setProviders(Collection<IDataProvider<T>> providers) {
        this.providers.addAll(providers);
    }

    public void setConflation(boolean conflate) {
        this.conflation = conflate;
    }

    @Override
    public void onData(final T data, IComponent from, final long receivedTime) {
        try {
            cache.onData(data, from, receivedTime);
            this.distributeData(data,from,receivedTime);
        } catch (Exception e) {
            logger.error("Error handling data",e);
        }
    }
    private ConcurrentMap<String,Boolean> conflationPendingMap = new ConcurrentHashMap<String,Boolean>(20);


    /**
     * Pass data to all acceptable receivers who were interested. Dispatching is
     * optional though advised (keyed on receiver name normally).
     * Conflation is optional.
     *
     * @param data
     * @param from
     * @param receivedTime
     */
    private void distributeData(final T data, final IComponent from, final long receivedTime) {
        Collection<ISubscriptionRequest<T>> matching = getMatchingRequests(data.getDataKey());
        if (matching == null) {
            return;
        }
        for (final ISubscriptionRequest<T> request:matching) {
            if (dispatcher != null) {
                if (conflation) {
                    if (null == conflationPendingMap.putIfAbsent(data.getDataKey(), Boolean.TRUE)) {
                        dispatch(request, data, receivedTime);
                    } //we found one for this key.
                } else {
                    dispatch(request,data,receivedTime);
                }

            } else {
                //on thread that provider gave us data on...
                request.getDataReceiver().onData(data,this,getTime());
            }
        }
    }

    private void dispatch(final ISubscriptionRequest request, final T data,final long receivedTime) {
        dispatcher.dispatch(request.getDataReceiver().getName(), new Runnable() {
            public void run() {
                try {
                    if (conflation) {
                        T latest = cache.snap(data.getDataKey());
                        conflationPendingMap.remove(data.getDataKey());
                        request.getDataReceiver().onData(latest, SubscriptionService.this, receivedTime);
                    } else {
                        //skip the cache get, adds nothing.
                        request.getDataReceiver().onData(data, SubscriptionService.this, receivedTime);
                    }
                } catch (Exception e) {
                    logger.error("Error distributing data", e);
                }
            }
        });
    }

    /**
     *
     * @return
     */
    public Collection<ISubscriptionRequest<T>> getMatchingRequests(String key) {
        return this.requests.get(key);
    }


    public void setTimeProvider(ITime time) {
        this.timeProvider = time;
    }
    public long getTime() {
        return timeProvider != null ? timeProvider.getNanoTime() : System.currentTimeMillis();
    }


}
