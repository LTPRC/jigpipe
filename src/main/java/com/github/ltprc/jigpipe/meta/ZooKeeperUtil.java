package org.lituo.jigpipe.meta;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import org.lituo.jigpipe.tool.BigpipeLogger;

/**
 * 封装了watcher和session失效自动重连的zookeeper的类，为了尽量减少对zookeeper服务的压力，使用单例模式
 */
public class ZooKeeperUtil implements Watcher {
    private final String connectionString;
    private final int sessionTimeoutms;
    private ZooKeeper zk;
    private static final Map<String, ZooKeeperUtil> instanceMap = new ConcurrentHashMap<String, ZooKeeperUtil>();

    protected static Logger logger = BigpipeLogger.getLogger();

    protected ZooKeeperUtil(String connectionString, int sessionTimeoutms) throws IOException {
        zk = new ZooKeeper(connectionString, sessionTimeoutms, this);
        this.connectionString = connectionString;
        this.sessionTimeoutms = sessionTimeoutms;
    }

    /**
     * Initialize Zookeeper instance.
     * 
     * @param clusterName
     * @param connectionString
     * @param sessionTimeoutms
     * @throws IOException
     */
    public static void init(String clusterName, String connectionString, int sessionTimeoutms) throws IOException {
        instanceMap.put(clusterName, new ZooKeeperUtil(connectionString, sessionTimeoutms));
    }

    /**
     * Release connection of specific cluster.
     */
    public static void release(String clusterName) {
        if (hasInstance(clusterName)) {
            ZooKeeperUtil instance = instanceMap.get(clusterName);
            try {
                instance.close();
            } catch (InterruptedException e) {
                logger.info("close zookeeper interruped", e);
            }
            instanceMap.remove(clusterName);
        }
    }

    /**
     * Check whether the named Zookeeper instance exists.
     * 
     * @return true if the named instance exists, otherwise false.
     */
    public static boolean hasInstance(String clusterName) {
        return null == instanceMap.get(clusterName);
    }

    /**
     * Get specific ZK instance
     * 
     * @return ZK instance
     */
    public static ZooKeeperUtil getInstance(String clusterName) {
        if (hasInstance(clusterName)) {
            ZooKeeperUtil instance = instanceMap.get(clusterName);
            if (instance != null) {
                return instance;
            }
        }
        throw new RuntimeException("ZooKeeper not initialized");
    }

    /**
     * ZK watcher process
     * 
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    public void process(WatchedEvent event) {
        if (event.getState() == KeeperState.SyncConnected) {
            synchronized (this) {
                this.notifyAll();
            }
        } else if (event.getState() == KeeperState.Expired) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                logger.info("close zookeeper interruped", e);
            }
            while (true) {
                try {
                    zk = new ZooKeeper(connectionString, sessionTimeoutms, this);
                    break;
                } catch (IOException e) {
                    logger.warn("zookeeper reinit failed", e);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.info("ignored sleep interrupt");
                }
            }
        }
    }

    /**
     * Get ZK info
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public byte[] get(String path) throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        return zk.getData(path, false, stat);
    }

    /**
     * Get ZK head info
     * @param path
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String getMeta(String path) throws KeeperException, InterruptedException {
        return new String(get(path), 8, get(path).length - 8);
    }

    public void waitConnected(long timeoutMs) throws InterruptedException {
        if (zk.getState() != ZooKeeper.States.CONNECTED) {
            synchronized (this) {
                this.wait(timeoutMs);
            }
        }
    }

    public void waitConnected() throws InterruptedException {
        if (zk.getState() != ZooKeeper.States.CONNECTED) {
            synchronized (this) {
                this.wait();
            }
        }
    }

    public void close() throws InterruptedException {
        zk.close();
    }
}
