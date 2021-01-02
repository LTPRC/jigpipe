package com.github.ltprc.jigpipe.meta;

public class DefaultRoleStrategy implements IRoleSelectStrategy {

    /**
     * Default set of roles. 1 stands for primary (broker) and 2 represents
     * secondary (broker).
     */
    private int[] roles = new int[] { 1, 2 };

    /**
     * Default set of weights for above roles. weights[i] stands for the weight of
     * roles[i].
     */
    private int[] weights = new int[] { 1, 3 };

    /** Index of the role to be chosen. */
    private int index = (int) Math.random() * roles.length;

    /** Time of the chosen role that has used. */
    private int count = 0;

    /** Roles are chosen one-by-one based on their different weights. (by lili19) */

    public int getCurrentRole() {
        if (count == weights[index]) {
            index = (index + 1) % roles.length;
            count = 1;
        }
        ++count;
        return roles[index];
    }

    public void setRoleSet(int[] roles, int[] weights) {
        index = (int) Math.random() * roles.length;
        count = 0;
        this.roles = roles;
        this.weights = weights;
    }

    public void setRoleSet(int index, int count, int[] roles, int[] weights) {
        this.index = index;
        this.count = count;
        this.roles = roles;
        this.weights = weights;
    }
}
