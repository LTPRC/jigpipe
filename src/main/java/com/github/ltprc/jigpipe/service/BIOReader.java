package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.CommandType;
import com.github.ltprc.jigpipe.command.MessageCommand;
import com.github.ltprc.jigpipe.command.SubscribeCommand;
import com.github.ltprc.jigpipe.exception.StripeEndException;
import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;

/**
 * BIO reader.
 */
public class BIOReader extends Reader {
    
    public BIOReader(String cluster) {
        super(cluster);
        client = new BIOClient();
    }
    
    public BIOClient createClient() {
        return new BIOClient();
    }

    public BIOClient getClient() {
        return (BIOClient) client;
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
        return connected;
    }

    /**
     * Send subscribe command and receive receiptCommand.
     * 
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public void doStartSubscribe() throws IOException, UnexpectedProtocol {
        SubscribeCommand subCmd = (SubscribeCommand) sendSubscribe();
        Packet receptPacket = this.getClient().receive();
        if (subCmd.getCommandType() != CommandType.BMQ_RECEIPT) {
            throw new UnexpectedProtocol(new Packet(subCmd), receptPacket);
        }
        SubscribeCommand receiptCommand = (SubscribeCommand) receptPacket.getCommand();
        if (!subCmd.getReceiptId().equals(receiptCommand.getReceiptId())) {
            throw new UnexpectedProtocol(new Packet(subCmd), receptPacket);
        }
    }

    /**
     * Receive packet with message from server and send ack command.
     * 
     * @return received packet
     * @throws IOException
     * @throws UnexpectedProtocol
     * @throws StripeEndException 
     */
    public Packet doReceivePacked() throws IOException, UnexpectedProtocol, StripeEndException {
        if (client.getCurrentAddress().getStripe().getEndPos() < getStartpoint()) {
            throw new StripeEndException(client.getCurrentAddress().getStripe(), getStartpoint());
        }
        Packet response = client.receive();
        if (response.getCommand().getCommandType() != CommandType.BMQ_MESSAGE) {
            throw new UnexpectedProtocol(null, response);
        }
        MessageCommand msgCommand = (MessageCommand) response.getCommand();
        responseMessage(msgCommand);
        this.setStartpoint(msgCommand.getTopicMessageId() + 1);
        return response;
    }

    /**
     * Receive packet with message from server and send ack command.
     * 
     * @return unpacked received packet
     * @throws IOException
     * @throws UnexpectedProtocol
     * @throws StripeEndException 
     */
    public StorePackage doReceive() throws IOException, UnexpectedProtocol, StripeEndException {
        return unpack(doReceivePacked());
    }
}
