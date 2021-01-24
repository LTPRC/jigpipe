package com.github.ltprc.jigpipe.service.reader;

import java.io.IOException;

import com.github.ltprc.jigpipe.bean.StorePackage;
import com.github.ltprc.jigpipe.exception.NameResolveException;
import com.github.ltprc.jigpipe.exception.ProtocolDisorderError;
import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;
import com.github.ltprc.jigpipe.service.Packet;
import com.github.ltprc.jigpipe.service.client.NonblockClient;
import com.github.ltprc.jigpipe.service.command.Command;
import com.github.ltprc.jigpipe.service.command.MessageCommand;

public class NonblockReader extends Reader {
    public static final int START = 1;
    public static final int CONNECTED = 2;
    public static final int RECEIVING = 4;

    private Packet lastPacket;
    private int protocolState = START;

    public NonblockReader(String cluster) {
        super(cluster);
    }

    public NonblockClient createClient() {
        return new NonblockClient();
    }

    public NonblockClient getClient() {
        return (NonblockClient) client;
    }

    public void open(String pipeletName, long startpoint) throws IOException, NameResolveException {
        super.open(pipeletName, startpoint);
        protocolState = START;
    }

    public Command sendConnect() throws IOException {
        if (protocolState != START) {
            throw new ProtocolDisorderError("try send subscribe in state " + protocolState);
        }
        Command command = super.sendConnect();
        lastPacket = new Packet(command);
        return command;
    }

    public Command sendSubscribe() throws IOException {
        if (protocolState != CONNECTED) {
            throw new ProtocolDisorderError("try send subscribe in state " + protocolState);
        }
        Command command = super.sendSubscribe();
        lastPacket = new Packet(command);
        return command;
    }

    /**
     * 尝试从底层网络读取数据，如果能读取到一条完整的协议，将按协议逻辑处理，如果是消息数据，将返回给调用者，
     * 并更新当前订阅点
     * @return 有消息数据时返回消息数据，否则返回null
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
            return unpackCapiPayload(packet);
        default:
            throw new UnexpectedProtocol(lastPacket, packet);
        }
        return null;
    }
}
