package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.component.BigpipeCommand;
import com.github.ltprc.jigpipe.component.BigpipePacket;
import com.github.ltprc.jigpipe.component.TopicAddress;

/**
 * Client class, used for managing connection and transmission.
 */
public interface IBigpipeClient {
    
    /**
     * Connect specific address.
     * @param addr
     * @throws IOException
     */
    public void connect(TopicAddress addr) throws IOException;
    
    /**
     * Get current connected address
     * @return
     */
    public TopicAddress getCurrentAddress();
    
    /**
     * Send command.
     * @param cmd
     * @throws IOException
     */
    public void send(BigpipeCommand cmd) throws IOException;
    
    /**
     * Receive packet(command + payloads).
     * @return
     * @throws IOException
     */
    public BigpipePacket receive() throws IOException;
    
    
    /**
     * Close connection.
     * @throws IOException
     */
    public void close() throws IOException;
}
