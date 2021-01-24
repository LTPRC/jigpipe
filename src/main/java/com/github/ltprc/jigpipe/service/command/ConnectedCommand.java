package com.github.ltprc.jigpipe.service.command;

public class ConnectedCommand extends Command {

    static {
        commandType = CommandType.BMQ_CONNECTED;
    }

    private long SessionMessageId;

    public long getSessionMessageId() {
        return SessionMessageId;
    }

    public void setSessionMessageId(long sessionMessageId) {
        SessionMessageId = sessionMessageId;
    }
}
