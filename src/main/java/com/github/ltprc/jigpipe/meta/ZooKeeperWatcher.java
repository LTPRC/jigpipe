package com.github.ltprc.jigpipe.meta;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

import com.github.ltprc.jigpipe.exception.MetaException;
import com.github.ltprc.jigpipe.tool.BigpipeLogger;

public class ZooKeeperWatcher implements Watcher {

    /**
     * Bigpipe cluster name
     */
    private static final String CLUSTER_NAME = "bigpipe_test_cluster";
    /**
     * 连接地址
     */
    private static final String ZK_ADDRESS = "192.168.137.128:2181";
    /**
     * 会话时间
     */
    private static final Integer ZK_SESSION_TIMEOUT = 2000;
    /**
     * Bigpipe logger
     */
    private static final Logger logger = BigpipeLogger.getLogger();
    private String clusterName;
    private String zkAddress;
    private Integer zkSessionTimeout;
    /**
     * ZK实例
     */
    private ZooKeeper zooKeeper;

    public ZooKeeperWatcher(String clusterName, String zkAddress, Integer zkSessionTimeout) {
        this.clusterName = clusterName;
        this.zkAddress = zkAddress;
        this.zkSessionTimeout = zkSessionTimeout;
        openConnection();
    }

    /**
     * 连接zk方法
     */
    public void openConnection(){
        try {
            if (hasInstance(clusterName)) {
                throw new MetaException("EX01");
            } else {
                zooKeeper = new ZooKeeper(zkAddress, zkSessionTimeout, this);
                MetaMap.getInstance().put(clusterName, this);
            }
            System.out.println("已发起面向集群" + clusterName + "连接的建立");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回调方法     监听连接，监听增删改节点事件
     * @param event
     */
    public void process(WatchedEvent event) {
        //获取当前状态
        KeeperState state = event.getState();
        //获取通知类型
        EventType type = event.getType();
        //获取节点路径
        String path = event.getPath();
        System.out.println("当前状态:"+state);
        System.out.println("通知类型:"+type);
        System.out.println("节点路径:"+path);
        //已经成功连接
        if (KeeperState.SyncConnected==state){
            if (Event.EventType.None==type){
                System.out.println("=============连接成功=============");
            }
            if (Event.EventType.NodeCreated==type){
                System.out.println("=============创建成功=============");
            }
            if (Event.EventType.NodeDataChanged==type){
                System.out.println("=============修改成功=============");
            }
            if (Event.EventType.NodeDeleted==type){
                System.out.println("=============删除成功=============");
            }
            synchronized (this) {
                this.notifyAll();
            }
        } else if (state == KeeperState.Expired) {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                logger.info("close zookeeper interruped", e);
            }
            /**
             * 无限重连
             */
            while (true) {
                try {
                    zooKeeper = new ZooKeeper(zkAddress, zkSessionTimeout, this);
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
     * Check whether the named ZooKeeper instance exists.
     * @param clusterName
     * @return true if the named instance exists, otherwise false.
     */
    public boolean hasInstance(String clusterName) {
        return MetaMap.getInstance().containsKey(clusterName);
    }

    /**
     * Get specific ZK instance
     * @param clusterName
     * @return ZK instance
     */
    public ZooKeeperWatcher getInstance(String clusterName) {
        return MetaMap.getInstance().get(clusterName);
    }

    public void waitConnected() throws InterruptedException {
        if (zooKeeper.getState() != ZooKeeper.States.CONNECTED) {
            synchronized (this) {
                this.wait();
            }
        }
    }

    public void waitConnected(long timeoutMs) throws InterruptedException {
        if (zooKeeper.getState() != ZooKeeper.States.CONNECTED) {
            synchronized (this) {
                this.wait(timeoutMs);
            }
        }
    }

    /**
     * Release connection
     * @param
     */
    public void release(){
        try {
            if(null != zooKeeper){
                zooKeeper.close();
            }
            MetaMap.getInstance().remove(clusterName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    /**
     * 创建节点
     */
    public void createNode(String path,String data){
        try {
            //启动监听
            zooKeeper.exists(path, true);
            //创建节点
            zooKeeper.create(path,data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("创建成功！！！");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改节点
     */
    public void setNode(String path,String data){
        try {
            //启动监听
            zooKeeper.exists(path, true);
            //修改节点
            zooKeeper.setData(path, data.getBytes(), -1);
            System.out.println("修改成功！！！");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除节点
     */
    public void deleteNode(String path){
        try {
            //启动监听
            zooKeeper.exists(path, true);
            //删除
            zooKeeper.delete(path, -1);
            System.out.println("删除成功！！！");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
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
        return zooKeeper.getData(path, false, stat);
    }

    public static void main(String[] args) throws InterruptedException, KeeperException {
        logger.info("test log4j");
        ZooKeeperWatcher mckz=new ZooKeeperWatcher(CLUSTER_NAME, ZK_ADDRESS, ZK_SESSION_TIMEOUT);
        //创建节点
//        mckz.createNode("/mckz","MCKZ");
        //修改
//        mckz.setNode("/mckz","wahahaha");
        //删除
//        mckz.deleteNode("/mckz");
        //关闭
//        mckz.closeConnection();
        mckz.waitConnected();
        System.out.println(mckz.getMeta("/bigpipe"));
    }
}
