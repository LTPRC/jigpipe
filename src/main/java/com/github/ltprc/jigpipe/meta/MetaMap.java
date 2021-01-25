package com.github.ltprc.jigpipe.meta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZooKeeper connection administration.
 * @author tuoli
 *
 */
public enum MetaMap {
    INSTANCE;

    /**
     * Map which stores active ZooKeeper connections
     */
    private final static Map<String, ZooKeeperWatcher> instanceMap = new ConcurrentHashMap<>();
    
    public Map<String, ZooKeeperWatcher> getInstance() {
        return instanceMap;
    }
}
