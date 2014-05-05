package com.oaktree.core.threading.dispatcher;

import com.oaktree.core.threading.dispatcher.mapped.DispatchTask;

import java.util.Collection;
import java.util.List;

/**
 * A processor of tasks.
 * Implementations may use queue/thread, disruptors etc.
 *
 * Created by IntelliJ IDEA.
 * User: IJ
 * Date: 21/02/12
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */
public interface ITaskProcessor extends Runnable {

    /**
     * Start the processor
     */
    public void start();

    /**
     * Stop the processor
     */
    public void stopProcessor();

    /**
     * Get the number of queued tasks
     * @return
     */
    public long getQueuedTasks();

    /**
     * Get the number of executed tasks
     * @return number of executed tasks
     */
    public long getExecutedTasks();

    /**
     * Get the queue processor name.
     * @return name of queue
     */
    public String getName();

    /**
     * Set the processor name
     * @param name
     */
    public void setName(String name);

    /**
     * Get all keys we are working
     * @return keys this processor knows about.
     */
    public Collection<String> getKeys();

    /**
     * Add a task for processing.
     * @param r
     */
    public void add(DispatchTask r);

    /**
     * Drain all tasks 
     */
    public void drain(List<DispatchTask> drainer);
}
