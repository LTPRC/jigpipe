package com.github.ltprc.jigpipe.meta;

/**
 * Broker is the server of the Baidu Bigpipe message queue system.
 *
 */
public class Broker {

    private String ip;

    private int port;

    private int role;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
