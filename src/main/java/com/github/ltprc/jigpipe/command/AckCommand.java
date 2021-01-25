package com.github.ltprc.jigpipe.command;

/**
 * ACK Command.
 * @author tuoli
 *
 */
public class AckCommand extends Command {

    static {
        commandType = CommandType.BMQ_ACK;
    }

    private String receiptId;
    private String destination;
    private int ackType;
    private long topicMessageId;
    private long sessionMessageId;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getAckType() {
        return ackType;
    }

    public void setAckType(int ackType) {
        this.ackType = ackType;
    }

    public long getTopicMessageId() {
        return topicMessageId;
    }

    public void setTopicMessageId(long topicMessageId) {
        this.topicMessageId = topicMessageId;
    }

    public long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    } 
}
