package com.github.ltprc.jigpipe.exception;

import org.apache.log4j.Logger;

import com.github.ltprc.jigpipe.service.Packet;
import com.github.ltprc.jigpipe.tool.BigpipeLogger;

/**
 * 报文协议异常。
 */
public class UnexpectedProtocol extends Exception {

    protected static Logger logger = BigpipeLogger.getLogger();

    private static final long serialVersionUID = 7565359552051015822L;

    private final Packet from;
    private final Packet to;

    public UnexpectedProtocol(Packet from, Packet to) {
        this.from = from;
        this.to = to;
    }

    public Packet getFrom() {
        return from;
    }

    public Packet getTo() {
        return to;
    }
}
