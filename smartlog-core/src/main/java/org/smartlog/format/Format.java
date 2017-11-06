package org.smartlog.format;

import org.smartlog.LogContext;

import javax.annotation.Nonnull;

public interface Format {
    /**
     * Convert log object to string presentation
     *
     * @param log log object
     * @return string message
     */
    String format(@Nonnull LogContext log);
}