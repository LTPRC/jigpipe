package com.github.ltprc.jigpipe.service;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.meta.TopicAddress;
import com.google.gson.Gson;

public class NIOClient implements IClient {

    private SocketChannel channel;
    private TopicAddress currentAddress;
    private ByteBuffer sendbuffer;
    private ByteBuffer readbufbody;
    private Queue<ByteBuffer> sendqueue = new LinkedList<ByteBuffer>();

    public TopicAddress getCurrentAddress() {
        return currentAddress;
    }

    public NIOClient() {
    }

    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * Create socket channel.
     * 
     * @return A brand-new channel instance without any active connection
     * @throws IOException
     */
    public SocketChannel createChannel() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        return channel;
    }

    /**
     * Create a TCP connection.
     * 
     * @param addr Address and stripe name resolved by nameService
     * @throws IOException
     */
    public void connect(TopicAddress addr) throws IOException {
        currentAddress = addr;
        if (channel == null || !channel.isOpen())
            createChannel();
        channel.connect(currentAddress.getAddress());
    }

    @Override
    public void send(Command command) throws IOException {
        Packet packet = new Packet();
        packet.setCommand(command);
        send(packet);
    }

    @Override
    public void send(Packet packet) throws IOException {
        Gson gson = new Gson();
        ByteBuffer newSendbuffer = ByteBuffer.allocate(gson.toJson(packet, Packet.class).getBytes().length);
        if (sendbuffer == null) {
            sendbuffer = newSendbuffer;
        } else {
            sendqueue.add(newSendbuffer);
        }
        flush();
    }

    /**
     * Flush byte message remained in buffer.
     * 
     * @return Byte number remained in buffer
     * @throws IOException
     */
    public int flush() throws IOException {
        while (sendbuffer != null) {
            channel.write(sendbuffer);
            if (sendbuffer.remaining() == 0) {
                sendbuffer = sendqueue.poll();
            } else {
                return sendbuffer.remaining();
            }
        }
        return 0;
    }

    /**
     * Get byte number remained in buffer
     * 
     * @return
     */
    public int remain() {
        if (sendbuffer == null) {
            return 0;
        }
        return sendbuffer.remaining();
    }

    @Override
    public Packet receive() throws IOException {
        if (readbufbody.remaining() > 0) {
            if (channel.read(readbufbody) < 0)
                throw new SocketException("socket may be closed by server");
            if (readbufbody.remaining() > 0)
                return null;
        }
        Gson gson = new Gson();
        Packet packet = gson.fromJson(readbufbody.array().toString(), Packet.class);
        return packet;
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            try {
                channel.close();
            } finally {
                sendbuffer = null;
                readbufbody = null;
            }
        }
    }
}
