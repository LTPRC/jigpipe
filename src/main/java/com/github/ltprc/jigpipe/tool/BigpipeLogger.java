package org.lituo.jigpipe.tool;

import org.apache.log4j.Logger;

public enum BigpipeLogger {
    LOGGER;
    private static Logger logger;
    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(BigpipeLogger.class);
        }
        return logger;
    }
}
