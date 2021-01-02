package org.lituo.jigpipe.exception;

/**
 * Stripe offset is out of range.
 */
public class StripeOffsetException extends NameResolveException {
    private static final long serialVersionUID = 1632017239727674430L;

    public StripeOffsetException(String pipeletName, long startpoint, long minoffset) {
        super(pipeletName, startpoint,
                "stripe offset " + startpoint + " is out of range[" + minoffset + ", " + Long.MAX_VALUE + ")");
    }

}
