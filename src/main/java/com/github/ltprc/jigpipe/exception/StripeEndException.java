package com.github.ltprc.jigpipe.exception;

import com.github.ltprc.jigpipe.bean.Stripe;

/**
 * 在当前stripe读完需要切换读下一个，需要应用层重连时抛出。
 */
public class StripeEndException extends Exception {
    private static final long serialVersionUID = 1793625294005939670L;

    public StripeEndException(Stripe s, long currentStartpoint) {
        super("current startpoint " + currentStartpoint 
                + " is out of current stripe " + s.getName() + "[" + s.getBeginPos()
                + ", " + s.getEndPos() + "]'s range, " + "try reconnect by current startpoint");
    }
}
