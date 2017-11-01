package org.smartlog;

import org.smartlog.format.Format;
import org.smartlog.output.Output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;

public class SmartLog {
    private static final ThreadLocal<LinkedList<LogContext>> CONTEXTS = ThreadLocal.withInitial(LinkedList::new);

    @Nonnull
    public static LogContext start(@Nonnull final Output output) {
        final LogContext context = new LogContext(output);
        CONTEXTS.get().push(context);
        return context;
    }

    public static void finish() {
        final LinkedList<LogContext> list = CONTEXTS.get();
        if (!list.isEmpty()) {
            final LogContext log = list.pop();

            log.output()
                    .write(log);

            // recover old MDC variables and old thread name
            log.endTime(System.currentTimeMillis())
                    .clearMDC()
                    .recoverThreadName();
        } else {
            throw new RuntimeException("Empty stack");
        }
    }

    @Nonnull
    protected static LogContext current() {
        final LinkedList<LogContext> list = CONTEXTS.get();
        if (!list.isEmpty()) {
            return list.getLast();
        } else {
            throw new RuntimeException("Empty stack");
        }
    }

    @Nonnull
    public static LogContext output(@Nonnull final Output output) {
        return current().output(output);
    }

    @Nonnull
    public static LogContext level(@Nonnull final LogLevel level) {
        return current().level(level);
    }

    @Nonnull
    public static LogContext format(@Nonnull final Format format) {
        return current().format(format);
    }

    @Nonnull
    public static LogContext title(@Nonnull final String title) {
        return current().title(title);
    }

    @Nonnull
    public static LogContext title(@Nonnull final String format, final Object... args) {
        return current().title(format, args);
    }

    @Nonnull
    public static LogContext attach(@Nonnull final String name, @Nullable final Object value) {
        return current().attach(name, value);
    }

    @Nonnull
    public static LogContext throwable(@Nonnull final Throwable newThrowable) {
        return current().throwable(newThrowable);
    }

    @Nonnull
    public static LogContext trace(final String msg) {
        return current().trace(msg);
    }

    @Nonnull
    public static LogContext trace(final String format, final Object... args) {
        return current().trace(format, args);
    }

    @Nonnull
    public static LogContext result(final Object result) {
        return current().result(result);
    }

    @Nonnull
    public static LogContext result(final LogLevel level, final Object result) {
        return current().result(level, result);
    }

    @Nonnull
    public static LogContext result(final LogLevel level, final Throwable throwable, final String description) {
        return current().result(level, throwable, description);
    }

    @Nonnull
    public static LogContext result(final LogLevel level, final Throwable throwable, final String description, final Object... args) {
        return current().result(level, throwable, description, args);
    }
}
