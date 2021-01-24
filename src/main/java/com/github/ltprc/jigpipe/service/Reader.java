package com.github.ltprc.jigpipe.service;

import java.io.IOException;
import java.util.ArrayList;

import com.github.ltprc.jigpipe.bean.StorePackage;
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
import com.github.ltprc.jigpipe.service.command.SubscribeCommand;

import io.netty.util.internal.StringUtil;

public abstract class Reader extends SessionLayer {

    private static int byteArrayToInt(byte[] buffer, int offset) {
        int rst = 0;
        for (int i = 0; i < 4; i++) {
            rst += ((buffer[offset + i] & 0xff) << i * 8);
        }
        return rst;
    }

    private long startpoint = JigpipeConstant.LATEST_OFFSET;

    public long getStartpoint() {
        return startpoint;
    }

    public void setStartpoint(long startpoint) {
        this.startpoint = startpoint;
    }

    public Reader(String cluster) {
        super(cluster);
    }

    /**
     * 根据pipelet名和偏移量获取服务器ip地址并且连接
     * 
     * @param pipeletName pipelet名，由pipe名 + 下划线 + pipelet号（从1开始）组成
     * @throws IOException
     * @throws NameResolveException
     */
    public void open(String pipeletName) throws IOException, NameResolveException {
        open(pipeletName, getStartpoint());
    }

    /**
     * 根据pipelet名和设定好的偏移量获取服务器ip地址并且连接
     * 
     * @param pipeletName pipelet名，由pipe名 + 下划线 + pipelet号（从1开始）组成
     * @param startpoint pipelet偏移量（订阅点）
     * @throws IOException
     * @throws NameResolveException
     */
    public void open(String pipeletName, long startpoint) throws IOException, NameResolveException {
        setPipeletName(pipeletName);
        if (getId() == null) {
            setId(generateDefaultId());
        }
        long position;
        if (startpoint == JigpipeConstant.OLDEST_OFFSET)
            position = 0;
        else if (startpoint == JigpipeConstant.LATEST_OFFSET) {
            position = Long.MAX_VALUE;
            this.startpoint = JigpipeConstant.LATEST_OFFSET;
        } else {
            position = startpoint;
        }
        TopicAddress address = nameService.lookup(getPipeletName(), position);
        if (startpoint == JigpipeConstant.OLDEST_OFFSET) {
            this.startpoint = address.getStripe().getBeginPos();
        }
        client.connect(address);
    }

    /**
     * 查找pipelet队首所在的服务器地址并且连接
     * 
     * @param pipeletName pipelet名，由pipe名 + 下划线 + pipelet号（从1开始）组成
     * @throws IOException
     * @throws NameResolveException
     */
    public void openAtHead(String pipeletName) throws IOException, NameResolveException {
        open(pipeletName, JigpipeConstant.OLDEST_OFFSET);
    }

    /**
     * 查找pipelet队尾所在的服务器地址并且连接
     * 
     * @param pipeletName
     *            pipelet名，由pipe名 + 下划线 + pipelet号（从1开始）组成
     * @throws IOException
     * @throws NameResolveException
     */
    public void openAtTail(String pipeletName) throws IOException, NameResolveException {
        open(pipeletName, JigpipeConstant.LATEST_OFFSET);
    }

    /**
     * 根据当前client属性向服务器发送连接命令
     * 
     * @return 当前包装的连接命令
     * @throws IOException
     */
    public Command sendConnect() throws IOException {
        ConnCommand connCmd = new ConnCommand();
        connCmd.setRole(JigpipeConstant.SUBSCRIBER_ROLE);
        connCmd.setSessionId(getId());
        if (!StringUtil.isNullOrEmpty(getUsername())) {
            connCmd.setUsername(getUsername());
            connCmd.setPassword(getPassword());
        }
        client.send(connCmd);
        return connCmd;
    }

    /**
     * 根据打包格式拆包
     * 
     * @param packet
     * @return
     * @throws UnexpectedProtocol
     */
    public StorePackage unpackCapiPayload(Packet packet) throws UnexpectedProtocol {
        if (packet.command.getCommandType() != CommandType.BMQ_MESSAGE) {
            throw new UnexpectedProtocol(null, packet);
        }
        int offset = 0;
        if (offset > packet.payload.length - 4) {
            throw new MalformedPackageException("package size is wrong: " + "offset " + offset + " of total " + packet.payload.length);
        }
        MessageCommand messageCommand = (MessageCommand) packet.command;
        byte[] payload = packet.payload;
        int packageLength = packet.payload.length;
        offset += 4;

        StorePackage pack = new StorePackage();
        pack.setStoreIndex(messageCommand.getTopicMessageId());
        while (offset < packageLength) {
            if (offset > packageLength - 4) {
                throw new MalformedPackageException("package offset is wrong: " + "offset " + offset + " of total " + packageLength);
            }
            int msgLen = byteArrayToInt(payload, offset);
            offset += 4;
            if (msgLen < 1 || msgLen > packageLength - offset) {
                throw new MalformedPackageException("message length " + msgLen + "/" + offset + " of " + pack.getMessages().size() + " in package is wrong");
            }
            byte[] tmp = new byte[msgLen];
            System.arraycopy(payload, offset, tmp, 0, msgLen);
            pack.getMessages().add(tmp);
            offset += msgLen;
        }
        pack.setMessages(new ArrayList<byte[]>());
        return pack;
    }

    /**
     * 根据当前client属性向服务器发送发起订阅的命令
     * 
     * @return 当前包装的发起订阅命令
     * @throws IOException
     */
    public Command sendSubscribe() throws IOException {
        SubscribeCommand subCmd = new SubscribeCommand();
        subCmd.setDestination(client.getCurrentAddress().getStripe().getName());
        subCmd.setStartpoint(startpoint);
        subCmd.setReceiptId(getId() + "-" + Math.random());
        client.send(subCmd);
        return subCmd;
    }

    /**
     * 向服务器回复对应消息的ack
     * 
     * @param messageComand 收到的消息报头
     * @return 当前回应的ack命令
     * @throws IOException
     */
    public Command responseMessage(MessageCommand messageComand) throws IOException {
        AckCommand ackCmd = new AckCommand();
        ackCmd.setDestination(messageComand.getDestination());
        ackCmd.setAckType(1);
        ackCmd.setTopicMessageId(messageComand.getTopicMessageId());
        ackCmd.setReceiptId(messageComand.getReceiptId());
        client.send(ackCmd);
        return ackCmd;
    }
}
