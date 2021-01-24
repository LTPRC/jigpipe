package com.github.ltprc.jigpipe.service.command;

public class AckCommand extends Command {

    static {
        commandType = CommandType.BMQ_ACK;
    }

    private String receiptId;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }
}
