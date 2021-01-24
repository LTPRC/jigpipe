package com.github.ltprc.jigpipe.service.command;

public class ConnectedCommand extends Command {

    static {
        commandType = CommandType.BMQ_CONNECTED;
    }

    private long sessionMessageId;

    public long getSessionMessageId() {
        return sessionMessageId;
    }

    public void setSessionMessageId(long sessionMessageId) {
        this.sessionMessageId = sessionMessageId;
    }
}
