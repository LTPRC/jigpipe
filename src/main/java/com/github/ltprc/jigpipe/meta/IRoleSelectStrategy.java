package com.github.ltprc.jigpipe.meta;

/**
 * Broker role selection strategy for Bigpipe consuming.
 *
 */
public interface IRoleSelectStrategy {
    public static final int BROKER_MASTER = 1;
    public static final int BROKER_SLAVE = 2;
    public int getCurrentRole();
}
