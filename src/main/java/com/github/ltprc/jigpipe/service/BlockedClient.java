package com.github.ltprc.jigpipe.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import com.github.ltprc.jigpipe.command.Command;
import com.github.ltprc.jigpipe.meta.TopicAddress;
import com.google.gson.Gson;

public class BlockedClient implements IClient {
    private Socket socket;
    private TopicAddress currentAddress;

    public BlockedClient() {
    }

    public Socket getSocket() {
        return socket;
    }

    public Socket createSocket() {
        socket = new Socket();
        return socket;
    }

    @Override
    public TopicAddress getCurrentAddress() {
        return currentAddress;
    }

    @Override
    public void connect(TopicAddress addr) throws IOException {
        currentAddress = addr;
        if (socket == null || socket.isClosed())
            createSocket();
        socket.connect(currentAddress.getAddress());
    }

    /**
     * TODO This is a mock method.
     * 发送携带附加数据的报文
     */
    @Override
    public void send(Command command) throws IOException {
        Packet packet = new Packet();
        packet.setCommand(command);
        send(packet);
    }

    /**
     * TODO This is a mock method.
     * 发送携带附加数据的报文
     */
    @Override
    public void send(Packet packet) throws IOException {
        Gson gson = new Gson();
        socket.getOutputStream().write(gson.toJson(gson, Packet.class).getBytes());
    }

    /**
     * TODO This is a mock method.
     * 接收一条完整报文
     * 
     * @return 报文对象，包括报头和附加数据
     * @throws IOException
     */
    @Override
    public Packet receive() throws IOException {
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String readLine = bufferedReader.readLine();
        Gson gson = new Gson();
        Packet packet = gson.fromJson(readLine, Packet.class);
        return packet;
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }
}
