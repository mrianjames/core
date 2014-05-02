package com.oaktree.core.id;

/**
 * Threadsafe getter of dispatch keys - you will probably only have one of these in a system
 * though its not mandated. Inject it whereever you need it and to maintain consistency.
 *
 * At its simplest this is merely a wrapper round an AtomicInteger.
 *
 * Oaktree Designs Ltd.
 * User: ij
 * Date: 25/07/11
 * Time: 19:48
 */
public interface IKeyGenerator {
    /**
     * Get the next available key. If no more available then
     * will throw IllegalStateException.
     * @return
     */
    public int next();
}
