package org.smartlog;

public enum TraceFlag {
    /**
     * only add trace
     */
    NONE,

    /**
     * add trace and set current time mark
     */
    MARK_TIME,

    /**
     * add trace with time diff between current time and time mark
     */
    WRITE_TIME,

    /**
     * works like WRITE_TIME and after reset time mark
     */
    WRITE_AND_MARK_TIME,
}