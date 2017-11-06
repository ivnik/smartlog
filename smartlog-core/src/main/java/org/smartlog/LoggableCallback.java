package org.smartlog;

public interface LoggableCallback {
    default void beforeLoggable() {
    }

    default void afterLoggable() {
    }
}
