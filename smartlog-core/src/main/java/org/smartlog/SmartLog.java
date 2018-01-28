package org.smartlog;

import org.smartlog.format.Format;
import org.smartlog.output.Output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.LinkedList;

public class SmartLog {
    private static final ThreadLocal<Deque<LogContext>> CONTEXTS = ThreadLocal.withInitial(LinkedList::new);

    /**
     * Start new log context and setup output
     *
     * @param output log output
     * @return new context
     */
    @Nonnull
    public static LogContext start(@Nonnull final Output output) {
        final LogContext ctx = new LogContext(output);
        CONTEXTS.get().push(ctx);

        return ctx;
    }

    /**
     * Start new log context
     *
     * @param loggableObject used by aspect to call before/after methods
     * @return new context
     */
    @Nonnull
    public static LogContext start(@Nonnull final Object loggableObject) {
        final Class clazz = Util.findRootEnclosingClass(loggableObject.getClass());
        final Output output = SmartLogConfig.getConfig().getDefaultOutput(clazz);
        return start(output, loggableObject);
    }

    /**
     * Start new log context
     *
     * @param output         output for current context
     * @param loggableObject used by aspect to call before/after methods
     * @return new context
     */
    @Nonnull
    public static LogContext start(@Nonnull final Output output, @Nullable final Object loggableObject) {
        final LogContext ctx = start(output)
                .loggableObject(loggableObject);

        if (loggableObject != null && loggableObject instanceof LoggableCallback) {
            final LoggableCallback callback = (LoggableCallback) loggableObject;

            try {
                callback.beforeLoggable();
            } catch (Exception e) {
                ctx.throwable(e);
            }
        }

        return ctx;
    }

    /**
     * Finish current context - write to output, reset mdc variables, reset thread name
     */
    public static void finish() {
        final Deque<LogContext> list = CONTEXTS.get();
        if (!list.isEmpty()) {
            final LogContext ctx = list.pop();

            final Object loggableObject = ctx.loggableObject();
            if (loggableObject != null && loggableObject instanceof LoggableCallback) {
                final LoggableCallback callback = (LoggableCallback) loggableObject;

                try {
                    callback.afterLoggable();
                } catch (Exception e) {
                    ctx.throwable(e);
                }
            }

            ctx.endTime(System.currentTimeMillis());
            ctx.output()
                    .write(ctx);

            // recover old MDC variables and old thread name
            ctx.clearMDC()
                    .recoverThreadName();
        } else {
            throw new RuntimeException("Loggable context is absent");
        }
    }

    /**
     * Returns current log context. If no context present method throws exception
     *
     * @return current context
     */
    @Nonnull
    public static LogContext current() {
        final Deque<LogContext> contextQueue = CONTEXTS.get();
        if (!contextQueue.isEmpty()) {
            return contextQueue.peek();
        } else {
            throw new RuntimeException("Loggable context is absent");
        }
    }

    /**
     * Set output
     *
     * @param output output
     * @return current context
     */
    @Nonnull
    public static LogContext output(@Nonnull final Output output) {
        return current().output(output);
    }

    /**
     * Set log level
     *
     * @param level log level
     * @return current context
     */
    @Nonnull
    public static LogContext level(@Nonnull final LogLevel level) {
        return current().level(level);
    }

    /**
     * Set output format
     *
     * @param format output format
     * @return current context
     */
    @Nonnull
    public static LogContext format(@Nonnull final Format format) {
        return current().format(format);
    }

    /**
     * Set title
     *
     * @param title title
     * @return current context
     */
    @Nonnull
    public static LogContext title(@Nonnull final String title) {
        return current().title(title);
    }

    /**
     * Set title using String.format
     *
     * @param format message format
     * @param args   message args
     * @return current context
     */
    @Nonnull
    public static LogContext title(@Nonnull final String format, final Object... args) {
        return current().title(format, args);
    }

    /**
     * Attach variable to current log context
     *
     * @param name  variable name
     * @param value value
     * @return current context
     */
    @Nonnull
    public static LogContext attach(@Nonnull final String name, @Nullable final Object value) {
        return current().attach(name, value);
    }

    /**
     * Add exception to current log context. Previous exceptions added as suppressed exceptions to last
     *
     * @param newThrowable exception
     * @return current context
     */
    @Nonnull
    public static LogContext throwable(@Nonnull final Throwable newThrowable) {
        return current().throwable(newThrowable);
    }

    /**
     * Add message trace
     *
     * @param msg trace message
     * @return current context
     */
    @Nonnull
    public static LogContext trace(final String msg) {
        return current().trace(msg);
    }

    /**
     * Add message trace using String.format
     *
     * @param format message format
     * @param args   message args
     * @return current context
     */
    @Nonnull
    public static LogContext trace(final String format, final Object... args) {
        return current().trace(format, args);
    }

    /**
     * Add message trace with trace flag
     *
     * @param flag additional action: (
     *             NONE - nothing,
     *             MARK_TIME - add trace and set current time mark,
     *             WRITE_TIME - add trace with time diff between current time and time mark,
     *             WRITE_AND_MARK_TIME - works like WRITE_TIME and after reset time mark
     *             )
     * @param msg  trace message
     * @return current context
     */
    @Nonnull
    public static LogContext trace(@Nonnull final TraceFlag flag, @Nonnull final String msg) {
        return current().trace(flag, msg);
    }

    /**
     * Add message trace using String.format and trace flag
     *
     * @param flag   additional action: (
     *               NONE - nothing,
     *               MARK_TIME - add trace and set current time mark,
     *               WRITE_TIME - add trace with time diff between current time and time mark,
     *               WRITE_AND_MARK_TIME - works like WRITE_TIME and after reset time mark
     *               )
     * @param format message format
     * @param args   message args
     * @return current context
     */
    @Nonnull
    public static LogContext trace(@Nonnull final TraceFlag flag, @Nonnull final String format, @Nonnull final Object... args) {
        return current().trace(flag, format, args);
    }

    /**
     * Set slf4j MDC variable. Old value will be reset after current context completion
     *
     * @param key   mdc variable name
     * @param value value
     * @return current context
     */
    public static LogContext pushMDC(@Nonnull final String key, @Nullable final String value) {
        return current().pushMDC(key, value);
    }

    /**
     * Set current thread name. Old name will be reset after current context completion
     *
     * @param newName new name
     * @return current context
     */
    public static LogContext threadName(@Nonnull final String newName) {
        return current().threadName(newName);
    }

    /**
     * Set result
     *
     * @param result current context log result
     * @return current context
     */
    @Nonnull
    public static LogContext result(final Object result) {
        return current().result(result);
    }

    /**
     * Set result using String.format
     *
     * @param format message format
     * @param args   message args
     * @return current context
     */
    @Nonnull
    public static LogContext result(final String format, final Object... args) {
        return current().result(format, args);
    }

    /**
     * Set result with log level
     *
     * @param level  log level
     * @param result result
     * @return current context
     */
    @Nonnull
    public static LogContext result(final LogLevel level, final Object result) {
        return current().result(level, result);
    }

    /**
     * Set result using String.format and log level
     *
     * @param level  log level
     * @param format message format
     * @param args   message args
     * @return current context
     */
    @Nonnull
    public static LogContext result(final LogLevel level, final String format, final Object... args) {
        return current().result(level, format, args);
    }

    /**
     * Set result with log level and exception
     *
     * @param level       log level
     * @param throwable   exception
     * @param description description
     * @return current context
     */
    @Nonnull
    public static LogContext result(final LogLevel level, final Throwable throwable, final String description) {
        return current().result(level, throwable, description);
    }

    /**
     * Set result with log level and exception
     *
     * @param level     log level
     * @param throwable exception
     * @param format    message format
     * @param args      message args
     * @return current context
     */
    @Nonnull
    public static LogContext result(final LogLevel level, final Throwable throwable, final String format, final Object... args) {
        return current().result(level, throwable, format, args);
    }
}
