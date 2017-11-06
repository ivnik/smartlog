package org.smartlog.format;

import org.smartlog.LogContext;

import javax.annotation.Nonnull;

/**
 *
 */
public class JsonFormat implements Format {
    static {
        // todo - check gson/jackson present and select one
    }

    @Override
    public String format(@Nonnull final LogContext log) {
        // todo - call real implementation
        return null;
    }

    private class GsonFormat {
        // todo
    }

    private class JacksonFormat {
        // todo
    }
}