package com.github.ltprc.jigpipe.meta;

import java.util.List;

public class Pipelet {
    private int id;
    private String name;
    private long pub_quota;
    private long sub_quota;
    private List<Stripe> stripes;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getPub_quota() {
        return pub_quota;
    }
    public void setPub_quota(long pub_quota) {
        this.pub_quota = pub_quota;
    }
    public long getSub_quota() {
        return sub_quota;
    }
    public void setSub_quota(long sub_quota) {
        this.sub_quota = sub_quota;
    }
    public List<Stripe> getStripes() {
        return stripes;
    }
    public void setStripes(List<Stripe> stripes) {
        this.stripes = stripes;
    }
}
