package com.oaktree.core.data.sequence;

import com.oaktree.core.container.IComponent;

/**
 * A component that takes data and passes on, after presumably some
 * transformation or use of that data.
 * Sequences can be wired together into a sequence pipeline for transforming
 * or mutating data from source input stage to output stage.
 * 
 * @author ij
 *
 * @param <I> - incoming data type
 * @param <O> - outgoing data type
 */
public interface IDataSequence<I,O> extends IComponent,IDataProvider<O>,IDataReceiver<I> {

}
