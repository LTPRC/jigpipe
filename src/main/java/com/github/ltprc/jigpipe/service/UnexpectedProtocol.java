package com.github.ltprc.jigpipe.service;

/**
 * Protocol is not as expected.
 * @author tuoli
 *
 */
public class UnexpectedProtocol extends Exception {

    private static final long serialVersionUID = 7565359552051015822L;

    private final Packet from;
    private final Packet to;

    public UnexpectedProtocol(Packet from, Packet to) {
        this.from = from;
        this.to = to;
        /**
         * TODO Process exception content
         */
    }

    public Packet getFrom() {
        return from;
    }

    public Packet getTo() {
        return to;
    }
}
