package com.github.ltprc.jigpipe.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * 用来存放不同集群ZK连接的单例散列表
 * @author tuoli
 *
 */
public enum MetaMap {
    METAMAP;
    
    /**
     * ZooKeeper散列表，用于存储属于不同集群的ZK实例
     */
    private final static Map<String, ZooKeeperWatcher> instanceMap = new HashMap<>();
    
    public static Map<String, ZooKeeperWatcher> getInstance() {
        return instanceMap;
    }
}
