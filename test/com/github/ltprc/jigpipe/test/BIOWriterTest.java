package com.github.ltprc.jigpipe.test;

import java.io.IOException;
import java.util.Date;

import com.github.ltprc.jigpipe.command.AckCommand;
import com.github.ltprc.jigpipe.constant.JigpipeConstant;
import com.github.ltprc.jigpipe.exception.NameResolveException;
import com.github.ltprc.jigpipe.exception.UnexpectedProtocol;
import com.github.ltprc.jigpipe.meta.ZooKeeperWatcher;
import com.github.ltprc.jigpipe.service.BIOWriter;
import com.github.ltprc.jigpipe.service.Packet;

public class BIOWriterTest {

    /** BlockedWriter instance used for demonstration. */
    private BIOWriter writer;

    /** Starting sequence id per testing. */
    private long lastTestEndPos = -1;

    /** Current sequence id per testing. */
    private long sendingSeq = 1;

    /**
     * Sending number for the demonstration.
     */
    private int testCount = 20;

    /** Sending time interval. */
    private int sendInteval = 100;

    public String nextMessage(long seq) {
        return new Date() + "#" + (seq - lastTestEndPos);
    }

    public void mainloop() {
        try {
            writer = new BIOWriter("test_cluster");
            writer.setUsername("USERNAME");
            writer.setPassword("********");
            writer.setPipeletName("topic01-1");
            /**
             * Session id must be set uniquely. Otherwise the default session id may cause
             * session conflict. The total amount of session ids should be maintained with a
             * limitation to keep the cluster zookeeper working appropriately.
             */
//                writer.setId("bigpipe4j-TestBlockedWriter-" + pipeletName);
            writer.open();
            writer.doConnect();
            /** Starting sequence id for the writer. */
            long serverLastSeq = writer.getSessionSeq() - 1;
            if (lastTestEndPos == -1)
                lastTestEndPos = serverLastSeq;
            if (sendingSeq < serverLastSeq) {
                System.out.println("skiped message from " + sendingSeq + " to " + serverLastSeq);
                sendingSeq = serverLastSeq + 1;
            }
            writer.setSessionSeq(sendingSeq);

            while (sendingSeq - lastTestEndPos <= testCount) {
                System.out.println("BlockingWriter: lastTestEndPos " + lastTestEndPos + " serverLastSeq "
                        + serverLastSeq + " sendingSeq " + sendingSeq);

                long startTime = System.currentTimeMillis();
                Packet packet = writer.doSend((new Date() + "#" + (sendingSeq - lastTestEndPos)).getBytes());
                long endTime = System.currentTimeMillis();

                if (packet != null) {
                    System.out.println((sendingSeq - lastTestEndPos) + " send " + sendingSeq + " store in "
                            + writer.getPipeletName() + ":" + ((AckCommand)packet.getCommand()).getTopicMessageId());
                }
                ++sendingSeq;
                long remainTime = sendInteval - (endTime - startTime);
                if (remainTime > 0) {
                    try {
                        Thread.sleep(remainTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("exception happend: " + e.getMessage() + " try reconnect");
        } catch (UnexpectedProtocol e) {
            System.err.println("exception happend: " + e.getMessage() + " try reconnect");
        } catch (NameResolveException e) {
            System.err.println("exception happend: " + e.getMessage() + " try reconnect");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ZooKeeperWatcher zkWatcher;
        try {
            zkWatcher = new ZooKeeperWatcher("test_cluster", "192.168.137.128:2181", 2000);
            zkWatcher.waitConnected();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("connect zookeeper failed");
            return;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                new BIOWriterTest().mainloop();
            }
        });
        t.start();
        try {
            t.join();
            zkWatcher.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
