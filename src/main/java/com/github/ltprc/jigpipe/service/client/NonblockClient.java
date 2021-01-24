package com.github.ltprc.jigpipe.service.client;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import com.github.ltprc.jigpipe.bean.Command;
import com.github.ltprc.jigpipe.bean.Packet;
import com.github.ltprc.jigpipe.bean.TopicAddress;
import com.google.gson.Gson;

public class NonblockClient implements IClient {
    private SocketChannel channel;
    private TopicAddress currentAddress;

    private ByteBuffer sendbuffer;
    private ByteBuffer readbufbody;
    private Queue<ByteBuffer> sendqueue = new LinkedList<ByteBuffer>();

    public NonblockClient() {
    }

    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * 用于获取尚未连接的SocketChannel以方便设置选项
     * 
     * @return 一个新的尚未连接的SocketChannel对象
     * @throws IOException
     */
    public SocketChannel createChannel() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        return channel;
    }

    /**
     * 获取当前连接到的stripe地址，在调用connect之后有效
     * 
     * @return 返回当前连接的地址，包括IP和stripe信息
     */
    public TopicAddress getCurrentAddress() {
        return currentAddress;
    }

    /**
     * 与指定stripe进行tcp连接
     * 
     * @param addr
     *            通过NameService获取的stripe地址
     * @throws IOException
     */
    public void connect(TopicAddress addr) throws IOException {
        currentAddress = addr;
        if (channel == null || !channel.isOpen())
            createChannel();
        channel.connect(currentAddress.getAddress());
    }

    /**
     * 发送携带附加数据的报文
     * 
     * @throws IOException
     */
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
     * 尝试发送当前缓冲中尚未发送的数据，并且返回本次发送之后的剩余数据量
     * 
     * @return 发送缓冲剩余未发送的字节数
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
     * 获取发送缓冲中还剩多少数据未写入socket
     * 
     * @return 剩余字节数
     */
    public int remain() {
        if (sendbuffer == null) {
            return 0;
        }
        return sendbuffer.remaining();
    }

    /**
     * TODO This is a mock method.
     * 接收一条完整报文
     * 
     * @return 报文对象，包括报头和附加数据
     * @throws IOException
     */
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
