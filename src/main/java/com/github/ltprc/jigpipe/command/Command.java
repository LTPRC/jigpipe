package com.github.ltprc.jigpipe.command;

/**
 * Command Abstract Class.
 * @author tuoli
 *
 */
public abstract class Command {

    static CommandType commandType;

    public CommandType getCommandType() {
        return commandType;
    }
}
