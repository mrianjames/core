package com.oaktree.core.id;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Atomic integer based key generator.
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 25/07/11
 * Time: 22:02
 */
public class AtomicIntKeyGenerator implements IKeyGenerator {
    private AtomicInteger ai = new AtomicInteger(0);
    @Override
    public int next() {
        return ai.incrementAndGet();
    }
}
