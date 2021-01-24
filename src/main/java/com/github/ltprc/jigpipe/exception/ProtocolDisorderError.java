package com.github.ltprc.jigpipe.exception;

/**
 * Protocol state is wrong, not START.
 */
public class ProtocolDisorderError extends RuntimeException {
    private static final long serialVersionUID = 6398582593692968608L;

    public ProtocolDisorderError(String msg) {
        super(msg);
    }
}
