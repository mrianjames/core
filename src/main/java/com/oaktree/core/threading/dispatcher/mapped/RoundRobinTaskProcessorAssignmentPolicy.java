package com.oaktree.core.threading.dispatcher.mapped;

import com.oaktree.core.threading.dispatcher.ITaskProcessor;
import com.oaktree.core.utils.CircularQueue;

import java.util.Iterator;

/**
 * Round robin over available processors.
 *
 * Created by IntelliJ IDEA.
 * User: IJ
 * Date: 22/02/12
 * Time: 07:13
 * To change this template use File | Settings | File Templates.
 */
public class RoundRobinTaskProcessorAssignmentPolicy implements ITaskProcessorAssignmentPolicy {
    private CircularQueue<ITaskProcessor> qps = new CircularQueue<ITaskProcessor>();
    private Iterator<ITaskProcessor> it;
    @Override
    public ITaskProcessor assign(String key, DispatchTask dispatchTask) {
        return it.next();
    }

    @Override
    public void setTaskProcessors(ITaskProcessor[] taskProcessors) {

        for (ITaskProcessor tprocessor:taskProcessors) {
            qps.add(tprocessor);
        }
        it = qps.iterator();

    }
}
