package com.github.ltprc.jigpipe.constant;

public class JigpipeConstant {

    /**
     * cluster name
     */
    public static final String CLUSTER_NAME = "bigpipe_test_cluster";
    /**
     * 连接地址
     */
    public static final String ZK_ADDRESS = "192.168.137.128:2181";


    /**
     * 会话时间
     */
    public static final Integer ZK_SESSION_TIMEOUT = 2000;

    public static final int BROKER_MASTER = 1;
    
    public static final int BROKER_SLAVE = 2;

    public static final int PUBLISHER_ROLE = 1;
    public static final int SUBSCRIBER_ROLE = 2;
    /** Message size limitation. */
    public static final int MAX_SEND_MESSAGE_LENGTH = 2097152;
}
