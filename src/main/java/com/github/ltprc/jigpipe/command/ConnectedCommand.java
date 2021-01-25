package com.github.ltprc.jigpipe.command;

/**
 * Connected Command
 * @author tuoli
 *
 */
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
