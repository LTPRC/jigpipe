package com.github.ltprc.jigpipe.service.command;

/**
 * TODO Mocked command bean
 */
public abstract class Command {

    static CommandType commandType;

    public CommandType getCommandType() {
        return commandType;
    }
}
