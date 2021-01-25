package com.github.ltprc.jigpipe.meta;

/**
 * Role selection strategy interface
 * @author tuoli
 *
 */
public interface IRoleStrategy {

    /**
     * Get selected broker role
     * @return selected role
     */
    public int getCurrentRole();
}
