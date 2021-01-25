package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.CommandType;
import com.github.ltprc.jigpipe.command.MessageCommand;
import com.github.ltprc.jigpipe.command.SubscribeCommand;
import com.github.ltprc.jigpipe.exception.StripeEndException;
import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;

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
     * 阻塞式地完成一次连接协议交互，验证身份信息
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public void doConnect() throws IOException, UnexpectedProtocol {
        Command connCmd = sendConnect();
        Packet connectedPacket = client.receive();
        if (connectedPacket.getCommand().getCommandType() != CommandType.BMQ_CONNECTED) {
            throw new UnexpectedProtocol(new Packet(connCmd), connectedPacket);
        }
    }

    /**
     * 阻塞式地完成一次发起订阅协议交互，此交互完成时，服务端将开始推送数据
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public void doStartSubscribe() throws IOException, UnexpectedProtocol {
        SubscribeCommand subCmd = (SubscribeCommand) sendSubscribe();
        Packet receptPacket = this.getClient().receive();
        if (subCmd.getCommandType() != CommandType.BMQ_RECEIPT) {
            throw new UnexpectedProtocol(new Packet(subCmd), receptPacket);
        }
        SubscribeCommand receptCommand = (SubscribeCommand) receptPacket.getCommand();
        if (!subCmd.getReceiptId().equals(receptCommand.getReceiptId())) {
            throw new UnexpectedProtocol(new Packet(subCmd), receptPacket);
        }
    }

    /**
     * 接收一条消息报文，并且向服务器回ack；成功的话，会更新当前订阅点
     * 
     * @return
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
     * 接收一条消息报文，并且向服务器回ack；成功的话，会更新当前订阅点
     * 
     * @return
     * @throws IOException
     * @throws UnexpectedProtocol
     * @throws StripeEndException 
     */
    public StorePackage doReceive() throws IOException, UnexpectedProtocol, StripeEndException {
        return unpackCapiPayload(doReceivePacked());
    }
}
