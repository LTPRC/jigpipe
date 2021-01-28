package com.github.ltprc.jigpipe.service;

import java.io.IOException;
import java.util.ArrayList;

import com.github.ltprc.jigpipe.command.AckCommand;
import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.CommandType;
import com.github.ltprc.jigpipe.command.ConnectCommand;
import com.github.ltprc.jigpipe.command.MessageCommand;
import com.github.ltprc.jigpipe.command.SubscribeCommand;
import com.github.ltprc.jigpipe.constant.ErrorConstant;
import com.github.ltprc.jigpipe.constant.JigpipeConstant;
import com.github.ltprc.jigpipe.meta.TopicAddress;

import io.netty.util.internal.StringUtil;

/**
 * Reader abstract class.
 * @author tuoli
 *
 */
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
     * Search address for connection.
     * 
     * @param pipeletName {pipeletName}_{pipeletNum}
     * @throws IOException
     */
    public void open(String pipeletName) throws IOException {
        open(pipeletName, getStartpoint());
    }

    /**
     * Search address for connection.
     * 
     * @param pipeletName {pipeletName}_{pipeletNum}
     * @param startpoint
     * @throws IOException
     */
    public void open(String pipeletName, long startpoint) throws IOException {
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
     * Search address for connection from the beginning of a pipelet.
     * 
     * @param pipeletName pipelet名，由pipe名 + 下划线 + pipelet号（从1开始）组成
     * @throws IOException
     */
    public void openAtHead(String pipeletName) throws IOException {
        open(pipeletName, JigpipeConstant.OLDEST_OFFSET);
    }

    /**
     * Search address for connection from the ending of a pipelet.
     * 
     * @param pipeletName
     *            pipelet名，由pipe名 + 下划线 + pipelet号（从1开始）组成
     * @throws IOException
     */
    public void openAtTail(String pipeletName) throws IOException {
        open(pipeletName, JigpipeConstant.LATEST_OFFSET);
    }

    /**
     * Send connect command.
     * 
     * @return
     * @throws IOException
     */
    public Command sendConnect() throws IOException {
        ConnectCommand connCmd = new ConnectCommand();
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
     * Unpack received packet.
     * 
     * @param packet
     * @return
     * @throws UnexpectedProtocol
     */
    public StorePackage unpack(Packet packet) throws UnexpectedProtocol {
        if (packet.getCommand().getCommandType() != CommandType.BMQ_MESSAGE) {
            throw new UnexpectedProtocol(null, packet);
        }
        int offset = 0;
        if (offset > packet.getPayload().length - 4) {
            throw new RuntimeException(ErrorConstant.ERR_WRONG_SIZE_MSG);
        }
        MessageCommand messageCommand = (MessageCommand) packet.getCommand();
        byte[] payload = packet.getPayload();
        int packageLength = packet.getPayload().length;
        offset += 4;

        StorePackage pack = new StorePackage();
        pack.setStoreIndex(messageCommand.getTopicMessageId());
        while (offset < packageLength) {
            if (offset > packageLength - 4) {
                throw new RuntimeException(ErrorConstant.ERR_WRONG_SIZE_MSG);
            }
            int msgLen = byteArrayToInt(payload, offset);
            offset += 4;
            if (msgLen < 1 || msgLen > packageLength - offset) {
                throw new RuntimeException(ErrorConstant.ERR_WRONG_SIZE_MSG);
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
     * Send subscribe command.
     * 
     * @return
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
     * Send ack command
     * 
     * @param messageComand
     * @return
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
