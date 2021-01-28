package com.github.ltprc.jigpipe.service;

import java.io.IOException;
import java.util.List;

import com.github.ltprc.jigpipe.command.AckCommand;
import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.command.CommandType;
import com.github.ltprc.jigpipe.command.ConnectCommand;
import com.github.ltprc.jigpipe.command.MessageCommand;
import com.github.ltprc.jigpipe.constant.ErrorConstant;
import com.github.ltprc.jigpipe.constant.JigpipeConstant;
import com.github.ltprc.jigpipe.meta.TopicAddress;

import io.netty.util.internal.StringUtil;

/**
 * Writer abstract class.
 * @author tuoli
 *
 */
public abstract class Writer extends SessionLayer {

    public Writer(String cluster) {
        super(cluster);
    }

    /**
     * Search address for connection.
     * 
     * @throws IOException
     */
    public void open() throws IOException {
        if (getId() == null) {
            setId(generateDefaultId());
        }
        TopicAddress address = nameService.lookupPub(getPipeletName());
        client.connect(address);
    }

    /**
     * Send connect command to the server.
     * 
     * @return connect command
     * @throws IOException
     */
    public Command sendConnect() throws IOException {
        ConnectCommand connCmd = new ConnectCommand();
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
     * Send unpacked binary data.
     * 
     * @param binary data
     * @param seq message id
     * @return message command
     * @throws IOException
     */
    public Command sendBinary(byte[] binary, long seq) throws IOException {
        if (binary == null || binary.length == 0) {
            throw new RuntimeException(ErrorConstant.ERR_WRONG_SIZE_MSG);
        }
        ByteBlockList byteBlockList = ByteBlockList.packRaw(binary);
        return sendPackedBinary(byteBlockList, seq);
    }

    /**
     * Send packed binary data.
     * 
     * @param binary data
     * @param seq message id
     * @return message command
     * @throws IOException
     */
    public Command sendPackedBinary(byte[] binary, long seq) throws IOException {
        if (binary == null || binary.length == 0) {
            throw new RuntimeException(ErrorConstant.ERR_WRONG_SIZE_MSG);
        }
        ByteBlockList byteBlockList = new ByteBlockList(binary);
        return sendPackedBinary(byteBlockList, seq);
    }

    /**
     * Send binary data pack.
     * 
     * @param payload data pack
     * @param seq message id
     * @return message command
     * @throws IOException
     */
    public Command sendPackedBinary(ByteBlockList payload, long seq) throws IOException {
        List<byte[]> byteList = payload.getList();
        for (byte[] bytes : byteList) {
            if (bytes == null || bytes.length == 0) {
                throw new RuntimeException(ErrorConstant.ERR_WRONG_SIZE_MSG);
            }
        }
        /** Check the size of payload. */
        if (payload.getBytesLength() >= JigpipeConstant.MAX_SEND_MESSAGE_LENGTH) {
            throw new RuntimeException(ErrorConstant.ERR_OVERSIZE_MSG);
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
     * Receive ACK from server in blocked way.
     * 
     * @return ack command
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet receiveAck() throws IOException, UnexpectedProtocol {
        return receiveAck(null);
    }

    /**
     * Receive ACK from server in blocked way.
     * 
     * @param messagePack
     * @return ack command
     * @throws IOException
     * @throws UnexpectedProtocol
     */
    public Packet receiveAck(Packet messagePack) throws IOException, UnexpectedProtocol {
        Packet ackPack = client.receive();
        if (ackPack.getCommand().getCommandType() != CommandType.BMQ_ACK) {
            throw new UnexpectedProtocol(messagePack, ackPack);
        }
        if (messagePack != null) {
            AckCommand ackCommand = (AckCommand) ackPack.getCommand();
            MessageCommand messageCommand = (MessageCommand) messagePack.getCommand();
            if (null == ackCommand.getReceiptId()) {
                ackCommand.setReceiptId("");
            }
            if (null == messageCommand.getReceiptId()) {
                ackCommand.setReceiptId("");
            }
            if (!ackCommand.getReceiptId().equals(messageCommand.getReceiptId())) {
                throw new UnexpectedProtocol(messagePack, ackPack);
            }
        }
        return ackPack;
    }
}