package com.github.ltprc.jigpipe.meta;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.ltprc.jigpipe.constant.JigpipeConstant;

/**
 * Default role selection strategy
 * @author tuoli
 *
 */
public class DefaultRoleStrategy implements IRoleStrategy {

    /**
     * Default weights of roles.
     */
    public static final int BROKER_MASTER_WEIGHT = 1;
    public static final int BROKER_SLAVE_WEIGHT = 3;

    private AtomicInteger index = new AtomicInteger();

    /**
     * Get selected broker role
     * The result would be either master of slave
     * @return selected role
     */
    public int getCurrentRole() {
        int rst = index.getAndAdd(1) % (BROKER_MASTER_WEIGHT + BROKER_SLAVE_WEIGHT);
        if (rst < BROKER_MASTER_WEIGHT) {
            return JigpipeConstant.BROKER_MASTER;
        } else {
            return JigpipeConstant.BROKER_SLAVE;
        }
    }
}
