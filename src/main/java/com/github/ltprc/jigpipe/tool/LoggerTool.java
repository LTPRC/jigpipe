package com.github.ltprc.jigpipe.tool;

import org.apache.log4j.Logger;

/**
 * Tool for log recording.
 * @author tuoli
 *
 */
public enum LoggerTool {
    LOGGER;
    private static Logger logger;
    public static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(LoggerTool.class);
        }
        return logger;
    }
}
