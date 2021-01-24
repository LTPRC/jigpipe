package com.github.ltprc.jigpipe.bean;

import java.util.List;

public class StorePackage {
    private long storeIndex;
    private List<byte[]> messages;

    public long getStoreIndex() {
        return storeIndex;
    }
    public void setStoreIndex(long storeIndex) {
        this.storeIndex = storeIndex;
    }
    public List<byte[]> getMessages() {
        return messages;
    }
    public void setMessages(List<byte[]> messages) {
        this.messages = messages;
    }
}
