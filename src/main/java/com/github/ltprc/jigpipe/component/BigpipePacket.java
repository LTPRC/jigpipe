package com.github.ltprc.jigpipe.component;

import com.github.ltprc.jigpipe.component.BigpipeCommand;

public class BigpipePacket {
    public BigpipeCommand command;
    public byte[] payload;
    
    public BigpipePacket() {
        
    }
    
    public BigpipePacket(BigpipeCommand cmd) {
        command = cmd;
    }
    
    public BigpipePacket(BigpipeCommand cmd, byte[] payload) {
        command = cmd;
        this.payload = payload;
    }
}
