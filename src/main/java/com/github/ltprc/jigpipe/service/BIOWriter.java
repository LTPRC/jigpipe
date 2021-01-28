package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.CommandType;
import com.github.ltprc.jigpipe.command.ConnectedCommand;

/**
 * BIO writer manages session sequence by itself.
 * Session sequence can be maintained by the SDK user to improve the efficiency.
 */
public class BIOWriter extends Writer {

    /** The session sequence id. */
    private long sessionSeq;

    public BIOWriter(String cluster) {
        super(cluster);
    }

    public BIOClient createClient() {
        return new BIOClient();
    }

    public BIOClient getClient() {
        return (BIOClient) client;
    }

    public long getSessionSeq() {
        return sessionSeq;
    }

    public void setSessionSeq(long sessionSeq) {
        this.sessionSeq = sessionSeq;
    }

    /**
     * Send connect command and receive connected command.
     * 
     * @return Received packet
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet doConnect() throws IOException, UnexpectedProtocol {
        Command connCmd = sendConnect();
        Packet connected = client.receive();
        if (connected.getCommand().getCommandType() != CommandType.BMQ_CONNECTED) {
            throw new UnexpectedProtocol(new Packet(connCmd), connected);
        }
        ConnectedCommand connectedCommand = (ConnectedCommand) connected.getCommand();
        sessionSeq = connectedCommand.getSessionMessageId() + 1;
        return connected;
    }

    /**
     * Send message command with packed data and receive ack command.
     * 
     * @param binaryMessage packed data
     * @return Received packet
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet doSendPacked(byte[] binaryMessage) throws IOException, UnexpectedProtocol {
        Command msgCmd = sendPackedBinary(binaryMessage, sessionSeq);
        Packet response = receiveAck(new Packet(msgCmd, binaryMessage));
        ++sessionSeq;
        return response;
    }

    /**
     * Send message command with unpacked data and receive ack command.
     * 
     * @param binaryMessage unpacked data
     * @return Received packet
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet doSend(byte[] binaryMessage) throws IOException, UnexpectedProtocol {
        Command msgCmd = sendBinary(binaryMessage, sessionSeq);
        Packet response = receiveAck(new Packet(msgCmd, binaryMessage));
        ++sessionSeq;
        return response;
    }
}
