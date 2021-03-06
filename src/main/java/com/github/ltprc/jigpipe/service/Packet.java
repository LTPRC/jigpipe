package com.github.ltprc.jigpipe.service;

import com.github.ltprc.jigpipe.command.Command;

public class Packet {
    
    /**
     * 报头
     */
    
    private Command command;
    /**
     * 附加数据列表
     */
    private byte[] payload;
    
    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public Packet() {}
    
    public Packet(Command cmd) {
        command = cmd;
    }
    
    public Packet(Command cmd, byte[] payload) {
        command = cmd;
        this.payload = payload;
    }
}
