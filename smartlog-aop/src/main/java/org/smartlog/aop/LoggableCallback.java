package org.smartlog.aop;

public interface LoggableCallback {
    default void beforeLoggable() {
    }

    default void afterLoggable() {
    }
}
