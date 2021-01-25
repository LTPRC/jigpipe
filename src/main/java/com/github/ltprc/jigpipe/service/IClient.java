package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.meta.TopicAddress;

/**
 * Client class used for managing connection and transmission.
 * @author tuoli
 *
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
     * Send packet(command).
     * @param cmd
     * @throws IOException
     */
    public void send(Command command) throws IOException;

    /**
     * Send packet(command + payloads).
     * @param cmd
     * @throws IOException
     */
    public void send(Packet packet) throws IOException;

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
