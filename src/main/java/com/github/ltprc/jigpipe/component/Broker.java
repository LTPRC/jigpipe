package org.lituo.jigpipe.component;

/**
 * Broker is the server of the Baidu Bigpipe message queue system.
 * @author tuoli
 *
 */
public class Broker {
    // Id of the broker group where the current broker belongs to.
    private String group;
    // Current broker ip.
    private String ip;
    // Current broker port.
    private int port;
    // Current broker name.
    private String name;
    // Current broker mport.
    private int mport;
    // Current broker role.
    private int role;
    
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getMport() {
        return mport;
    }
    public void setMport(int mport) {
        this.mport = mport;
    }
    public int getRole() {
        return role;
    }
    public void setRole(int role) {
        this.role = role;
    }
}
