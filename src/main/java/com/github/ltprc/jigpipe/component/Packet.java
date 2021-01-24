package com.github.ltprc.jigpipe.component;

import com.github.ltprc.jigpipe.component.Command;

public class Packet {
    public Command command;
    public byte[] payload;
    
    public Packet() {
        
    }
    
    public Packet(Command cmd) {
        command = cmd;
    }
    
    public Packet(Command cmd, byte[] payload) {
        command = cmd;
        this.payload = payload;
    }
}
