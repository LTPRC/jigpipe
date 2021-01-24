package com.github.ltprc.jigpipe.service;

import java.io.IOException;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.CommandType;
import com.github.ltprc.jigpipe.command.ConnectedCommand;
import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;

/**
 * <p>
 * 使用本类实现的方法将是同步的发送方式且用的阻塞IO，想实现异步的发送方式可以使用底层的分离发送方法，但仍然是阻塞的IO。
 * </p>
 * <p>
 * 本类的方法内部会管理session_msg_id，适用于对每一次发送与上一次前因后果逻辑联系很强的情况
 * </p>
 * <p>
 * 使用底层的方法实现异步发送的方式能大幅提高吞吐量但是需要自己管理session_msg_id
 * </p>
 */
public class BIOWriter extends Writer {

    /** The session sequence id. */
    private long sessionSeq;

    public BIOWriter(String cluster) {
        super(cluster);
    }

    public BlockedClient createClient() {
        return new BlockedClient();
    }

    public BlockedClient getClient() {
        return (BlockedClient) client;
    }

    public long getSessionSeq() {
        return sessionSeq;
    }

    public void setSessionSeq(long sessionSeq) {
        this.sessionSeq = sessionSeq;
    }

    /**
     * 阻塞式地完成一次bigpipe协议的连接协议交互，验证身份信息； 协议成功将将更新内部session_seq
     * 
     * @return 返回服务器响应的bigpipe报文，其中包含本会话id上一次断开时在服务端保留的会话消息id
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
     * 按c-api打包协议打包消息后，组装消息报文发送一条消息，等待接收到服务器ack后返回
     * 
     * @param message 按c-api打包协议打包的文本消息
     * @return 服务器返回的ack报文
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet doSendPacked(String message) throws IOException, UnexpectedProtocol {
        return doSendPacked(message.getBytes());
    }

    /**
     * 按c-api打包协议打包消息后，组装消息报文发送一条消息，等待接收到服务器ack后返回
     * 
     * @param binaryMessage 按c-api打包协议打包的二进制数据
     * @return 服务器返回的ack报文
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
     * 组装消息报文发送一条消息，等待接收到服务器ack后返回
     * 
     * @param binaryMessage 待发送的二进制数据
     * @return 服务器返回的ack报文
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
