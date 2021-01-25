package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.command.AckCommand;
import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.ConnectedCommand;
import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;

/**
 * NIO writer.
 * @author tuoli
 *
 */
public class NIOWriter extends Writer {

    private long sendingSeq;
    private long ackedSeq;

    public NIOWriter(String cluster) {
        super(cluster);
    }

    public NIOClient createClient() {
        return new NIOClient();
    }

    public NIOClient getClient() {
        return (NIOClient) client;
    }

    public long getSendingSeq() {
        return sendingSeq;
    }

    public void setSendingSeq(long sendingSeq) {
        this.sendingSeq = sendingSeq;
    }

    public long getAckedSeq() {
        return ackedSeq;
    }

    public void setAckedSeq(long ackedSeq) {
        this.ackedSeq = ackedSeq;
    }

    /**
     * Send packed message.
     * <strong style="color: red">Before</strong> ack command arriving from the server, update sendingSeq automatically
     * 
     * @param binaryMessage
     * @return
     * @throws IOException
     */
    public Command sendPackedMessage(byte[] binaryMessage) throws IOException {
        return sendPackedBinary(binaryMessage, sendingSeq++);
    }

    /**
     * Send unpacked message.
     * <strong style="color: red">Before</strong> ack command arriving from the server, update sendingSeq automatically
     * 
     * @param binaryMessage
     * @return
     * @throws IOException
     */
    public Command sendMessage(byte[] binaryMessage) throws IOException {
        return sendBinary(binaryMessage, sendingSeq++);
    }

    /**
     * Receive command from the server.
     * If connected command is received, automatically updating sendingseq and ackedseq.
     * If ack command is received, automatically updating ackedseq.
     * 
     * @return valid command or null
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet doReceive() throws IOException, UnexpectedProtocol {
        Packet packet = client.receive();
        if (packet == null) {
            return null;
        }
        switch (packet.getCommand().getCommandType()) {
        case BMQ_CONNECTED:
            ConnectedCommand connectedCommand = (ConnectedCommand) packet.getCommand();
            long serverAckedSeq = connectedCommand.getSessionMessageId();
            if (ackedSeq < serverAckedSeq)
                ackedSeq = serverAckedSeq;
            sendingSeq = ackedSeq + 1;
            break;
        case BMQ_ACK:
            AckCommand ackCommand = (AckCommand) packet.getCommand();
            ackedSeq = ackCommand.getSessionMessageId();
            break;
        default:
            throw new UnexpectedProtocol(null, packet);
        }
        return packet;
    }
}
