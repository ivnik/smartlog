package org.smartlog.output;

import org.smartlog.LogContext;

/**
 * 
 */
public interface Output {
    void write(LogContext log);
}