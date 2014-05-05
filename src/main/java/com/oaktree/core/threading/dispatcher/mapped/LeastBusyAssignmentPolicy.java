package com.oaktree.core.threading.dispatcher.mapped;

import java.util.ArrayList;
import java.util.List;

import com.oaktree.core.threading.dispatcher.ITaskProcessor;

/**
 * Return the least busy of queues in terms of executed count
 * TODO should we also look at queue size?
 *
 * Created by IntelliJ IDEA.
 * User: IJ
 * Date: 22/02/12
 * Time: 07:38
 * To change this template use File | Settings | File Templates.
 */
public class LeastBusyAssignmentPolicy implements ITaskProcessorAssignmentPolicy{
    private List<ITaskProcessor> processors = new ArrayList<ITaskProcessor>();

    @Override
    public ITaskProcessor assign(String key, DispatchTask dispatchTask) {
        long leastExec = Long.MAX_VALUE;
        ITaskProcessor leastBusy = null;
        for (ITaskProcessor p:processors) {
            if (p.getExecutedTasks() < leastExec) {
                leastExec = p.getExecutedTasks();
                leastBusy = p;
            }
        }
        return leastBusy;
    }

    @Override
    public void setTaskProcessors(ITaskProcessor[] taskProcessors) {
        for (ITaskProcessor processor:taskProcessors) {
        	processors.add(processor);
        }
    }
}
