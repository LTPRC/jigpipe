package com.github.ltprc.jigpipe.service.writer;

import java.io.IOException;

import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;
import com.github.ltprc.jigpipe.service.ByteBlockList;
import com.github.ltprc.jigpipe.service.Packet;
import com.github.ltprc.jigpipe.service.client.NonblockClient;
import com.github.ltprc.jigpipe.service.command.AckCommand;
import com.github.ltprc.jigpipe.service.command.Command;
import com.github.ltprc.jigpipe.service.command.CommandType;
import com.github.ltprc.jigpipe.service.command.ConnectedCommand;

public class NonblockWriter extends Writer {

    private long sendingSeq;
    private long ackedSeq;

    public NonblockWriter(String cluster) {
        super(cluster);
    }

    public NonblockClient createClient() {
        return new NonblockClient();
    }

    public NonblockClient getClient() {
        return (NonblockClient) client;
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
     * 按c-api打包协议打包消息后，组装消息报文发送一条消息，<strong style="color: red">不</strong>等待服务器ack，将自增sendingSeq
     * 
     * @param binaryMessage 按c-api打包协议打包的二进制数据
     * @return 返回组装好的message报文对象
     * @throws IOException
     */
    public Command sendPackedMessage(byte[] binaryMessage) throws IOException {
        return sendPackedBinary(binaryMessage, sendingSeq++);
    }

    /**
     * 组装消息报文发送一条消息，<strong style="color: red">不</strong>等待服务器ack，与老式c-api不兼容，老式c-api和java-api将无法订阅，将自增sendingSeq
     * 
     * @param binaryMessage 待发送的二进制数据
     * @return 返回组装好的message报文对象
     * @throws IOException
     */
    public Command sendMessage(byte[] binaryMessage) throws IOException {
        return sendBinary(binaryMessage, sendingSeq++);
    }

    /**
     * 尝试从底层网络读取数据，如果能读取到一条完整的协议，将按协议逻辑处理； 收到connected报文后将更新内部sendingseq和ackedseq
     * 收到ack报文后将更新内部的ackedseq
     * 
     * @return 接收到完整的报文时返回报文，否则返回null
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
