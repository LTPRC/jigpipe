package com.github.ltprc.jigpipe.service;

import java.io.IOException;
import java.util.List;

import com.github.ltprc.jigpipe.bean.TopicAddress;
import com.github.ltprc.jigpipe.constant.JigpipeConstant;
import com.github.ltprc.jigpipe.exception.MalformedPackageException;
import com.github.ltprc.jigpipe.exception.NameResolveException;
import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;
import com.github.ltprc.jigpipe.service.command.AckCommand;
import com.github.ltprc.jigpipe.service.command.Command;
import com.github.ltprc.jigpipe.service.command.CommandType;
import com.github.ltprc.jigpipe.service.command.ConnCommand;
import com.github.ltprc.jigpipe.service.command.MessageCommand;

import io.netty.util.internal.StringUtil;

/**
 * <p>
 * 包含对会话基本信息的设置，以及对分离的（异步）发送协议的方法支持
 * </p>
 */
public abstract class Writer extends SessionLayer {

    public Writer(String cluster) {
        super(cluster);
    }

    /**
     * 查找预先设定的pipelet队尾所在的提供发布的服务器地址并且连接
     * 
     * @throws IOException
     * @throws NameResolveException
     */
    public void open() throws IOException, NameResolveException {
        if (getId() == null) {
            setId(generateDefaultId());
        }
        TopicAddress address = nameService.lookupPub(getPipeletName());
        client.connect(address);
    }

    /**
     * 根据当前client属性向服务器发送连接命令
     * 
     * @return 当前包装的连接命令
     * @throws IOException
     */
    public Command sendConnect() throws IOException {
        ConnCommand connCmd = new ConnCommand();
        connCmd.setRole(JigpipeConstant.PUBLISHER_ROLE);
        connCmd.setSessionId(getId());
        if (!StringUtil.isNullOrEmpty(getUsername())) {
            connCmd.setUsername(getUsername());
            connCmd.setPassword(getPassword());
        }
        client.send(connCmd);
        return connCmd;
    }

    /**
     * 将单条二进制消息按照c-api打包协议格式打包 c-api打包协议格式： (Total size including itself) + (size 1)
     * + (content 1) + ... + (size N) + (content N)
     * 
     * @param binary
     *            输入的二进制流
     * @return 打包的二进制流集合
     */
    public ByteBlockList capiStylePackMessage(byte[] binary) {
        ByteBlockList block = new ByteBlockList();
        byte[] binaryPack = new byte[binary.length + 8];
        block.appendInt(binaryPack.length);
        block.appendInt(binary.length);
        block.appendByteArray(binary);
        return block;
    }

    /**
     * 按格式打包，消息报文发送二进制消息
     * 
     * @param binary 二进制消息
     * @param seq 在本次会话中消息对应的id
     * @return 返回组装好的消息报文的报头
     * @throws IOException
     */
    public Command sendBinary(byte[] binary, long seq) throws IOException {
        if (binary == null || binary.length == 0) {
            throw new MalformedPackageException("Empty content is not allowed.");
        }
        ByteBlockList payload = new ByteBlockList();
        payload.appendInt(8 + binary.length);
        payload.appendInt(binary.length);
        payload.appendByteArray(binary);
        return sendBinary(payload, seq);
    }

    /**
     * 按格式打包，消息报文发送二进制消息
     * 
     * @param payload 二进制消息
     * @param seq 在本次会话中消息对应的id
     * @return 返回组装好的消息报文的报头
     * @throws IOException
     */
    public Command sendBinary(ByteBlockList payload, long seq) throws IOException {
        List<byte[]> byteList = payload.getList();
        for (byte[] bytes : byteList) {
            if (bytes == null || bytes.length == 0) {
                throw new MalformedPackageException("Empty content is not allowed.");
            }
        }
        /** Check the size of payload. */
        if (payload.getBytesLength() >= JigpipeConstant.MAX_SEND_MESSAGE_LENGTH) {
            throw new MalformedPackageException("Size limitation " + JigpipeConstant.MAX_SEND_MESSAGE_LENGTH + " is exceeded.");
        }
        MessageCommand msgCmd = new MessageCommand();
        msgCmd.setDestination(client.getCurrentAddress().getStripe().getName());
        msgCmd.setNoDup(false);
        msgCmd.setSessionMessageId(seq);
        msgCmd.setReceiptId(getId() + "-" + Math.random());
        msgCmd.setSessionId(getId());
        msgCmd.setMessageLength(payload.getBytesLength());
        Packet packet = new Packet();
        client.send(packet);
        return msgCmd;
    }

    /**
     * 阻塞至接收到完整的服务器响应报文为止，如果不是ack报文则抛出异常
     * 
     * @return 服务器响应的ack报文
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet receiveAck() throws IOException, UnexpectedProtocol {
        return receiveAck(null);
    }

    /**
     * 阻塞至接收到完整的服务器响应报文为止，如果不是ack报文则抛出异常
     * 
     * @param messagePack 之前发送出去的消息报文，可以为null
     * @return 服务器响应的ack报文
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet receiveAck(Packet messagePack) throws IOException, UnexpectedProtocol {
        Packet ackPack = client.receive();
        if (ackPack.command.getCommandType() != CommandType.BMQ_ACK) {
            throw new UnexpectedProtocol(messagePack, ackPack);
        }
        if (messagePack != null) {
//            && !ackPack.command.getReceiptId().equals(messagePack.command.getReceiptId())) {
            AckCommand ackCommand = (AckCommand) ackPack.command;
            throw new UnexpectedProtocol(messagePack, ackPack);
        }
        return ackPack;
    }
}