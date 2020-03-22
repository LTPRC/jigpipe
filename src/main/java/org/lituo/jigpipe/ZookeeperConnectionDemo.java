package org.lituo.jigpipe;

import org.lituo.jigpipe.meta.ZooKeeperUtil;

public class ZookeeperConnectionDemo {
    public static final String CLUSTERNAME = "Bigpipe-cluster-Huawei";
    public static final String ZOOKEEPERSTRING = "localhost:8191";
    public static final int SESSIONTIMEOUTMS = 2000;

    public static void main(String[] args) {
        //1.Connect ZK and get meta info.
        connectZooKeeper(CLUSTERNAME, ZOOKEEPERSTRING, SESSIONTIMEOUTMS, false);
        Thread t = new Thread(new Runnable() {
            public void run() {
                System.out.println("Jigpipe can work here...");
                //2. Connect Bigpipe broker
                //3. Work(write/read) on brokers
            }
        });
        t.start();
        try {
            t.join();
            ZooKeeperUtil.getInstance(CLUSTERNAME).close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void connectZooKeeper(String clusterName, String zooKeeperString, int sessionTimeoutMs, 
            boolean isWaitConnected) {
        try {
            ZooKeeperUtil.init(clusterName, zooKeeperString, sessionTimeoutMs);
            if (isWaitConnected) {
                ZooKeeperUtil.getInstance(zooKeeperString).waitConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("connect zookeeper failed");
            return;
        }
    }
}
