package com.github.ltprc.jigpipe.service;

import java.util.ArrayList;
import java.util.List;

public class ByteBlockList {

    private static byte[] intToByteArray(int n) {
        byte[] buffer = new byte[4];
        for (int i = 0; i < 4; i++) {
            buffer[i] = (byte) ((n >> i * 8) & 0xff);
        }
        return buffer;
    }

    private List<byte[]> list = new ArrayList<byte[]>();
    private int bytesLength = 0;

    public int getBytesLength() {
        return bytesLength;
    }

    public List<byte[]> getList() {
        return list;
    }

    public void appendInt(int n) {
        byte[] intBytes = intToByteArray(n);
        list.add(intBytes);
        bytesLength += 4;
    }

    public void replaceInt(int n, int index) {
        byte[] oldBytes = list.get(index);
        byte[] newBytes = intToByteArray(n);
        list.set(index, newBytes);
        bytesLength += 4 - oldBytes.length;
    }

    public void appendByteArray(byte[] bytes) {
        list.add(bytes);
        bytesLength += bytes.length;
    }

    public void replaceByteArray(byte[] newBytes, int index) {
        byte[] oldBytes = list.get(index);
        list.set(index, newBytes);
        bytesLength += newBytes.length - oldBytes.length;
    }
}
