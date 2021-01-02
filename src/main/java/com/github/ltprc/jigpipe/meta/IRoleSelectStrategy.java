package com.github.ltprc.jigpipe.meta;

/**
 * Broker role selection strategy for Bigpipe consuming.
 *
 */
public interface IRoleSelectStrategy {
    public int getCurrentRole();
}
