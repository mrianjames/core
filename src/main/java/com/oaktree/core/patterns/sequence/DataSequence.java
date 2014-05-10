/**
 * 
 */
package com.oaktree.core.patterns.sequence;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.oaktree.core.container.IComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oaktree.core.container.AbstractComponent;
import com.oaktree.core.container.ComponentState;
import com.oaktree.core.container.ComponentType;

/**
 * A DataSequence is a component that acts as an step in a data transformation pipeline. 
 * For example, if you wish to subscribe to 3 sources of similar data type, 
 * perform some mutation on this data and hand off to a publisher of this data in another. Each stage - data receive,transform,
 * publish can be a sequence of arbitary complexity.
 * A real-world use case: We have a data feed handler - it receives stock prices from an external TCP connection. We need
 * to take that data, apply a spread calculation to these values, and re-distrubute it on an internal bus middle-ware. We therefore
 * define our data type as type StockPrice. 
 * The first sequence is the "source" of the data - the component that takes the TCP data, and makes the StockPrice object. 
 * The second sequence is the "transform" - we take the StockPrice and apply a numerical increment to the "price" field. 
 * The third sequence is the "publish" - we take the amended data object, transform it to a new internal protocol and write to a multicast socket.
 * 
 * At each stage we are not limited to one receiver of data. Nor are we limited to one provider of data. In the above example
 * the transform may receive data from 3 sources, and hand off to 2 publishers.  
 * 
 * We define the parlance "source" to mean an input source of data. This is a provider of data, an IDataProvider<T>
 * We define the parlance "sink" to mean an output/publisher of data. This is a receiver of data, an IDataReceiver<T>
 *  
 *  @param <I> - input data type.
 *  @param <O> - output data type.
 *  
 * @author ij
 *
 */
public class DataSequence<I,O> extends AbstractComponent implements IDataSequence<I,O> {

	public DataSequence(String name) {
		setName(name);
		setComponentType(ComponentType.SERVICE);
		setComponentSubType("data.sequence");
	}
	
	/**
	 * If true then log data entry point at info level.
	 */
	private boolean verbose = false;
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
		if (logger.isInfoEnabled()) {
			logger.info(getName() + " verbose: "+ this.verbose);
		}
	}


	protected final static Logger logger = LoggerFactory.getLogger(DataSequence.class);
	
	
	/**
	 * Listeners/Receivers of our data we receive and mutate/translate.
	 */
	private List<IDataReceiver<O>> receivers = new ArrayList<IDataReceiver<O>>();

	/**
	 * Log out incoming data
	 */
	private boolean log = false;
	
	/**
	 * Set if we should enable log printing (toggle verbose with other flag).
	 * @param log
	 */
	public void setLog(boolean log) {
		this.log = log;
		if (logger.isInfoEnabled()) {
			logger.info(getName() + " log: " + this.log);
		}
	}
	
	@Override
	public void initialise() {
		this.setState(ComponentState.INITIALISING);
		this.onInitialise();
		this.setState(ComponentState.INITIALISED);
	}
	
	@Override
	public void start() {
		this.setState(ComponentState.STARTING);
		this.onStart();
		this.setState(ComponentState.AVAILABLE);
	}
	
	protected void onStart() {}
	protected void onInitialise() {}
	protected void onStop() {}

	@Override
	public void stop() {
		this.setState(ComponentState.STOPPING);
		this.onStop();		
		this.setState(ComponentState.STOPPED);
	}

	@Override
	public void addDataReceiver(IDataReceiver<O> receiver) {
		receivers.add(receiver);
	}

	@Override
	public void setDataReceiver(IDataReceiver<O> receiver) {
		addDataReceiver(receiver);
	}

	@Override
	public Collection<IDataReceiver<O>> getReceivers() {
		return receivers; //mutable. don't molest me. given in good faith to avoid extra objects.
	}

	@Override
	public void removeReceiver(IDataReceiver<O> receiver) {
		receivers.remove(receiver);
	}

    @Override
    public void registerInterest(Object key, IDataReceiver<O> from) {

    }

    @Override
    public void removeInterest(Object key, IDataReceiver<O> from) {

    }

    @Override
	public void onData(I data, IComponent from, long receivedTime) {
		//do what you need with the data...then distribute the result.
		O updated = process(data,from,receivedTime);
		for (IDataReceiver<O> sink: receivers) {
			sink.onData(updated, this, receivedTime);
		}
	}

	/**
	 * Do some work with the data. Default job is to log at trace
	 * and return same object. You can do what you want here - 
	 * mutate, return different object of same type etc.
	 * 
	 * Simple example assumes input and output data types are identical.
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected O process(I data, IComponent from, long receivedTime) {
		if (log ) {
			log(data,from,receivedTime);
		}
		return (O)data;
	}
	
	/**
	 * Helpful print. 
	 * @param data
	 * @param from
	 * @param receivedTime
	 */
	protected void log(I data, IComponent from, long receivedTime) {
		if (!log) {
			return;
		}
		if (!verbose && logger.isTraceEnabled()) {
			logger.trace("onData["+from+"]: "+ data +" @ "+receivedTime);
		} else if (verbose && logger.isInfoEnabled()) {
			logger.info("onData["+from+"]: "+ data +" @ "+receivedTime);
		}
	}
	
}
