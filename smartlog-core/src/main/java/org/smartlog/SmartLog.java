package org.smartlog;

import org.smartlog.format.Format;
import org.smartlog.output.Output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.LinkedList;

public class SmartLog {
    private static final ThreadLocal<Deque<LogContext>> CONTEXTS = ThreadLocal.withInitial(LinkedList::new);

    @Nonnull
    public static LogContext start(@Nonnull final Output output) {
        final LogContext ctx = new LogContext(output);
        CONTEXTS.get().push(ctx);

        return ctx;
    }

    @Nonnull
    public static LogContext start(@Nonnull final Object loggableObject) {
        final Class clazz = Util.findRootEnclosingClass(loggableObject.getClass());
        final Output output = SmartLogConfig.getConfig().getDefaultOutput(clazz);
        return start(output, loggableObject);
    }

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

    @Nonnull
    public static LogContext current() {
        final Deque<LogContext> contextQueue = CONTEXTS.get();
        if (!contextQueue.isEmpty()) {
            return contextQueue.peek();
        } else {
            throw new RuntimeException("Loggable context is absent");
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
    public static LogContext trace(@Nonnull final TraceFlag flag, @Nonnull final String msg) {
        return current().trace(flag, msg);
    }

    @Nonnull
    public static LogContext trace(@Nonnull final TraceFlag flag, @Nonnull final String msg, @Nonnull final Object... args) {
        return current().trace(flag, msg, args);
    }

    @Nonnull
    public static LogContext result(final Object result) {
        return current().result(result);
    }

    @Nonnull
    public static LogContext result(final String description, final Object... args) {
        return current().result(description, args);
    }

    @Nonnull
    public static LogContext result(final LogLevel level, final Object result) {
        return current().result(level, result);
    }

    @Nonnull
    public static LogContext result(final LogLevel level, final String description, final Object... args) {
        return current().result(level, description, args);
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
