package org.lituo.jigpipe.exception;

/**
 * Unable to obtain information (including broker groups, stripes, etc.) while
 * resolving the name.
 */
public class NameResolveException extends Exception {
    private static final long serialVersionUID = 6066322969565260318L;

    private String pipeletName;
    private long startpoint;

    public String getPipeletName() {
        return pipeletName;
    }

    public long getStartpoint() {
        return startpoint;
    }

    public NameResolveException(String pipeletName, long startpoint, String msg) {
        super(msg);
        this.pipeletName = pipeletName;
        this.startpoint = startpoint;
    }
}
