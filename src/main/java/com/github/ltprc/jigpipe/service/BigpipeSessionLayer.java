package org.lituo.jigpipe.service;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.lituo.jigpipe.meta.NameService;
import org.lituo.jigpipe.tool.BigpipeLogger;

/**
 * <p>
 * 用于实现bigpipe协议的session功能，从而在服务端保存一些客户端的状态；
 * 每一个客户端连接session_id是要求唯一的，用于区分每一个客户端连接
 * </p>
 * <p>
 * 基类包含一个最基本的session_id生成的实现，应用层也可以自行设置
 * </p>
 * <p>
 * 此外，这一层封装了传输协议层和名称解析模块，规范了最基本的对应用提供的接口
 * </p>
 */
public abstract class BigpipeSessionLayer {
    private NameService nameService;
    private IBigpipeClient client;
    private String id;
    private String username;
    private String password;
    private String pipeletName;

    private static AtomicLong objectId = new AtomicLong(0);

    protected static Logger logger = BigpipeLogger.getLogger();

    public BigpipeSessionLayer(String cluster) {
        nameService = new NameService(cluster);
        client = createClient();
    }

    public String getHostName() {
        String hostname = "unknownhost";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostname = addr.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostname;
    }

    /**
     * Mock default session id generation.
     * @return
     */
    public String generateDefaultId() {
        return "SESSIONID" + objectId.incrementAndGet();
    }

    public abstract IBigpipeClient createClient();

    public NameService getNameService() {
        return nameService;
    }

    public void setNameService(NameService nameserver) {
        this.nameService = nameserver;
    }

    public IBigpipeClient getClient() {
        return client;
    }

    public void setClient(IBigpipeClient client) {
        this.client = client;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPipeletName() {
        return pipeletName;
    }

    public void setPipeletName(String pipeletName) {
        this.pipeletName = pipeletName;
    }

}
