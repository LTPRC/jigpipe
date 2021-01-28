package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.MessageCommand;
import com.github.ltprc.jigpipe.constant.ErrorConstant;

/**
 * NIO reader.
 * @author tuoli
 *
 */
public class NIOReader extends Reader {
    public static final int START = 1;
    public static final int CONNECTED = 2;
    public static final int RECEIVING = 4;

    private Packet lastPacket;
    private int protocolState = START;

    public NIOReader(String cluster) {
        super(cluster);
    }

    public NIOClient createClient() {
        return new NIOClient();
    }

    public NIOClient getClient() {
        return (NIOClient) client;
    }

    public void open(String pipeletName, long startpoint) throws IOException {
        super.open(pipeletName, startpoint);
        protocolState = START;
    }

    @Override
    public Command sendConnect() throws IOException {
        if (protocolState != START) {
            throw new RuntimeException(ErrorConstant.ERR_WRONG_PROTOCOL_STATUS);
        }
        Command command = super.sendConnect();
        lastPacket = new Packet(command);
        return command;
    }

    @Override
    public Command sendSubscribe() throws IOException {
        if (protocolState != CONNECTED) {
            throw new RuntimeException(ErrorConstant.ERR_WRONG_PROTOCOL_STATUS);
        }
        Command command = super.sendSubscribe();
        lastPacket = new Packet(command);
        return command;
    }

    /**
     * Receive command from the server.
     * If connected command is received, automatically updating status and subscribe.
     * If receipt command is received, automatically updating status.
     * If message command is received, automatically updating status and start point, send response.
     * 
     * @return valid command or null
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public StorePackage doReceive() throws IOException, UnexpectedProtocol {
        Packet packet = client.receive();
        if (packet == null)
            return null;
        switch (packet.getCommand().getCommandType()) {
        case BMQ_CONNECTED:
            protocolState = CONNECTED;
            lastPacket = new Packet(sendSubscribe());
            break;
        case BMQ_RECEIPT:
            protocolState = RECEIVING;
            lastPacket = null;
            break;
        case BMQ_MESSAGE:
            MessageCommand messageCommand = (MessageCommand) packet.getCommand();
            lastPacket = new Packet(responseMessage(messageCommand));
            this.setStartpoint(messageCommand.getTopicMessageId() + 1);
            return unpack(packet);
        default:
            throw new UnexpectedProtocol(lastPacket, packet);
        }
        return null;
    }
}
