package com.github.ltprc.jigpipe.command;

public class SubscribeCommand extends Command {
    static {
        commandType = CommandType.BMQ_SUBSCRIBE;
    }

    private String destination;
    private long startpoint;
    private String receiptId;

    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public long getStartpoint() {
        return startpoint;
    }
    public void setStartpoint(long startpoint) {
        this.startpoint = startpoint;
    }
    public String getReceiptId() {
        return receiptId;
    }
    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }
}
