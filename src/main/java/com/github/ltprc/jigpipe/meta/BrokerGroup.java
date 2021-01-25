package com.github.ltprc.jigpipe.meta;

import java.util.List;

/**
 * Broker group POJO
 * @author tuoli
 *
 */
public class BrokerGroup {
    private String name;

    private int status;

    private List<Broker> brokers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Broker> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<Broker> brokers) {
        this.brokers = brokers;
    }
}
