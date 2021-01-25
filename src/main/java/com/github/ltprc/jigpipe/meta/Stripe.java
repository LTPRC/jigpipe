package com.github.ltprc.jigpipe.meta;

/**
 * Stripe POJO.
 * @author tuoli
 *
 */
public class Stripe {
    private int id;
    
    private String name;
    
    private long beginPos;
    
    private long endPos;
    
    private long endTimestamp;
    
    private String servingGroup;

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

    public long getBeginPos() {
        return beginPos;
    }

    public void setBeginPos(long beginPos) {
        this.beginPos = beginPos;
    }

    public long getEndPos() {
        return endPos;
    }

    public void setEndPos(long endPos) {
        this.endPos = endPos;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getServingGroup() {
        return servingGroup;
    }

    public void setServingGroup(String servingGroup) {
        this.servingGroup = servingGroup;
    }
}
