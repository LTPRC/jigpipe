package com.github.ltprc.jigpipe.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.github.ltprc.jigpipe.meta.NameService;
import com.github.ltprc.jigpipe.service.client.IClient;
import com.github.ltprc.jigpipe.tool.BigpipeLogger;

/**
 * <p>
 * 实现session功能，从而在服务端保存一些客户端的状态；
 * 每一个客户端连接session_id是要求唯一的，用于区分每一个客户端连接
 * </p>
 * <p>
 * 基类包含一个最基本的session_id生成的实现，应用层也可以自行设置
 * </p>
 * <p>
 * 此外，这一层封装了传输协议层和名称解析模块，规范了最基本的对应用提供的接口
 * </p>
 */
public abstract class SessionLayer {
    private NameService nameService;
    private IClient client;
    private String id;
    private String username;
    private String password;
    private String pipeletName;

    private static AtomicLong objectId = new AtomicLong(0);

    protected static Logger logger = BigpipeLogger.getLogger();

    public SessionLayer(String cluster) {
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

    public abstract IClient createClient();

    public NameService getNameService() {
        return nameService;
    }

    public void setNameService(NameService nameserver) {
        this.nameService = nameserver;
    }

    public IClient getClient() {
        return client;
    }

    public void setClient(IClient client) {
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
