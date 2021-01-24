package com.github.ltprc.jigpipe.service.command;

public class MessageCommand extends Command {

    static {
        commandType = CommandType.BMQ_MESSAGE;
    }

    private String destination;

    private boolean noDup;

    private long sessionMessageId;

    private String receiptId;

    private String sessionId;

    private int messageLength;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean getNoDup() {
        return noDup;
    }

    public void setNoDup(boolean noDup) {
        this.noDup = noDup;
    }

    public long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }
}
