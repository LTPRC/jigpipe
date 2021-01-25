package com.github.ltprc.jigpipe.constant;

/**
 * Constants for SDK configurations
 * @author tuoli
 *
 */
public class JigpipeConstant {

    /**
     * Broker roles
     * Master: 1
     * Slave: 2
     */
    public static final int BROKER_MASTER = 1;
    public static final int BROKER_SLAVE = 2;

    /**
     * Client roles
     * Publisher: 1
     * Subscriber: 2
     */
    public static final int PUBLISHER_ROLE = 1;
    public static final int SUBSCRIBER_ROLE = 2;
    /**
     * Special offset definitions
     * Oldest offset: -2
     * Latest offset: -1
     */
    public static final long OLDEST_OFFSET = -2;
    public static final long LATEST_OFFSET = -1;
    /** 
     * Message size limitation
     */
    public static final int MAX_SEND_MESSAGE_LENGTH = 2097152;
    public static final int MAX_RECV_BUFFER_LENGTH = 8388608;
}
