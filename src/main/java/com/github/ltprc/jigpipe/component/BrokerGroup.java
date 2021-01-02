package com.github.ltprc.jigpipe.component;

import java.util.List;

public class BrokerGroup {
    private List<Broker> brokers;
    
    private int epoch;
    
    private String name;
    
    private int repair_last_data;
    
    private int status;
    
    private int to_delete;
    
    private long to_delete_update_timestamp;
    
    public List<Broker> getBrokers() {
        return brokers;
    }
    public void setBrokers(List<Broker> brokers) {
        this.brokers = brokers;
    }
    public int getEpoch() {
        return epoch;
    }
    public void setEpoch(int epoch) {
        this.epoch = epoch;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getRepair_last_data() {
        return repair_last_data;
    }
    public void setRepair_last_data(int repair_last_data) {
        this.repair_last_data = repair_last_data;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public int getTo_delete() {
        return to_delete;
    }
    public void setTo_delete(int to_delete) {
        this.to_delete = to_delete;
    }
    public long getTo_delete_update_timestamp() {
        return to_delete_update_timestamp;
    }
    public void setTo_delete_update_timestamp(long to_delete_update_timestamp) {
        this.to_delete_update_timestamp = to_delete_update_timestamp;
    }
}
