package com.github.ltprc.jigpipe.exception;

/**
 * A invalid parameter is provided.
 */
public class InvalidParameter extends RuntimeException {
    private static final long serialVersionUID = -6283247724607093234L;

    public InvalidParameter(String msg) {
        super(msg);
    }
}
