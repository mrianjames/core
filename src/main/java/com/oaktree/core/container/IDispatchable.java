package com.oaktree.core.container;

import com.oaktree.core.threading.policy.IThreadingPolicy;

/**
 * Interface to describe something that can be dispatched to.
 */
public interface IDispatchable {

    /**
     * Inject the desired threading policy.
     * @param policy
     */
    public void setThreadingPolicy(IThreadingPolicy policy);

    /**
     * Get the configured threading policy.
     * @return
     */
    public IThreadingPolicy getThreadingPolicy();
}
