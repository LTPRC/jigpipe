package com.github.ltprc.jigpipe.command;

/**
 * Receipt Command
 * @author tuoli
 *
 */
public class ReceiptCommand extends Command {

    static {
        commandType = CommandType.BMQ_RECEIPT;
    }

    private String receiptId;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }
}
