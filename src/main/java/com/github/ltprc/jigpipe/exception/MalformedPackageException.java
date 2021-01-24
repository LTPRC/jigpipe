package com.github.ltprc.jigpipe.exception;

/**
 * 数据包的大小或报头内容出现异常。
 */
public class MalformedPackageException extends RuntimeException {
    private static final long serialVersionUID = -713194396180373324L;

    public MalformedPackageException(String msg) {
        super(msg);
    }
}
