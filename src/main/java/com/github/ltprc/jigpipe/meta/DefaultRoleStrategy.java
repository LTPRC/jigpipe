package com.github.ltprc.jigpipe.meta;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultRoleStrategy implements IRoleSelectStrategy {

    /**
     * Default weights of above roles.
     */
    public static final int BROKER_MASTER_WEIGHT = 1;
    public static final int BROKER_SLAVE_WEIGHT = 3;

    /** Index of the role to be chosen. */
    private AtomicInteger index = new AtomicInteger();

    public int getCurrentRole() {
        int rst = index.getAndAdd(1) % (BROKER_MASTER_WEIGHT + BROKER_SLAVE_WEIGHT);
        if (rst < BROKER_MASTER_WEIGHT) {
            return BROKER_MASTER;
        } else {
            return BROKER_SLAVE;
        }
    }
}
