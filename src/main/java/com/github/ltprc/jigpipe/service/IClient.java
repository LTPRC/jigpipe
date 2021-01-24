package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.component.Command;
import com.github.ltprc.jigpipe.component.Packet;
import com.github.ltprc.jigpipe.component.TopicAddress;

/**
 * Client class, used for managing connection and transmission.
 */
public interface IClient {
    
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
    public void send(Command cmd) throws IOException;
    
    /**
     * Receive packet(command + payloads).
     * @return
     * @throws IOException
     */
    public Packet receive() throws IOException;
    
    
    /**
     * Close connection.
     * @throws IOException
     */
    public void close() throws IOException;
}
