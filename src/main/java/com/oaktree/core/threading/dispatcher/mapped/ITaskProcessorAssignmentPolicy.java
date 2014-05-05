package com.oaktree.core.threading.dispatcher.mapped;

import com.oaktree.core.threading.dispatcher.ITaskProcessor;

/**
 * Created by IntelliJ IDEA.
 * User: IJ
 * Date: 22/02/12
 * Time: 07:10
 * To change this template use File | Settings | File Templates.
 */
public interface ITaskProcessorAssignmentPolicy {
    /**
     * Assign a task to a task processor.
     *
     * @param key
     * @param dispatchTask
     */
    public ITaskProcessor assign(String key, DispatchTask dispatchTask);

    /**
     * Set the available task processors.
     *
     * @param taskProcessors
     */
    public void setTaskProcessors(ITaskProcessor[] taskProcessors);
}
