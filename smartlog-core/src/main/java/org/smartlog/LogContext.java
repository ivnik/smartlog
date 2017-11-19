package org.smartlog;

import org.slf4j.MDC;
import org.smartlog.format.Format;
import org.smartlog.output.Output;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

import static java.lang.System.currentTimeMillis;

/**
 *
 */
@NotThreadSafe
public class LogContext implements AutoCloseable {
    private final long startTime = currentTimeMillis();
    private long endTime = 0;

    @Nonnull
    private Output output;

    @Nullable
    private Object loggableObject;

    /**
     * Log level
     */
    @Nullable
    private LogLevel level;

    /**
     * Log message format
     */
    @Nullable
    private Format format;

    /**
     * Title attribute
     */
    @Nullable
    private String title;

    @Nullable
    private Object[] titleArgs;

    /**
     * Result attribute
     */
    @Nullable
    private Object result;

    /**
     * Throwable attribute
     */
    @Nullable
    private Throwable throwable;

    @Nullable
    private ArrayList<Throwable> suppressedThrowables;

    /**
     * Time mark for trace
     */
    private long timeMark = startTime;

    /**
     * Trace
     */
    @Nullable
    private StringBuilder trace;

    /**
     * Replaced mdc elements
     */
    @Nullable
    private Deque<MDCEntry> mdcStack = null;

    /**
     * Context (id -> object)
     */
    @Nullable
    private Map<String, Object> attrs = null;

    /**
     * Old thread name
     */
    @Nullable
    private String oldThreadName;

    protected LogContext(@Nonnull final Output output) {
        this.output = output;
    }

    public Output output() {
        return output;
    }

    public LogContext output(final Output output) {
        this.output = output;
        return this;
    }

    @Nullable
    public Object loggableObject() {
        return loggableObject;
    }

    public LogContext loggableObject(@Nullable final Object loggableObject) {
        this.loggableObject = loggableObject;
        return this;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }

    protected LogContext endTime(final long endTime) {
        this.endTime = endTime;
        return this;
    }

    @Nullable
    public String title() {
        if (titleArgs != null && title != null) {
            title = String.format(title, titleArgs);
            titleArgs = null;
        }

        return title;
    }

    @Nonnull
    public LogContext title(@Nullable final String title) {
        this.title = title;
        return this;
    }

    @Nonnull
    public LogContext title(@Nullable final String title, final Object... titleArgs) {
        this.title = title;
        this.titleArgs = titleArgs;
        return this;
    }

    @Nullable
    public LogLevel level() {
        return level;
    }

    public LogContext level(@Nullable final LogLevel level) {
        this.level = level;
        return this;
    }

    @Nullable
    public Format format() {
        return format;
    }

    public LogContext format(@Nullable final Format format) {
        this.format = format;
        return this;
    }

    @Nonnull
    public LogContext attach(@Nonnull final String name, @Nullable final Object value) {
        if (attrs == null) {
            attrs = new HashMap<>();
        }

        attrs.put(name, value);
        return this;
    }

    @Nullable
    public Object getAttr(@Nonnull final String name) {
        return attrs != null ? attrs.get(name) : null;
    }

    @Nullable
    public Object result() {
        return result;
    }

    public LogContext result(@Nullable final Object result) {
        this.result = result;
        return this;
    }

    @Nonnull
    public LogContext result(final String description, final Object... args) {
        return result(String.format(description, args));
    }

    @Nonnull
    public LogContext result(final LogLevel level, final Object result) {
        return level(level)
                .result(result);
    }

    @Nonnull
    public LogContext result(final LogLevel level, final String description, final Object... args) {
        return level(level)
                .result(String.format(description, args));
    }

    @Nonnull
    public LogContext result(final LogLevel level, final Throwable throwable, final String description) {
        return level(level)
                .throwable(throwable)
                .result(description);
    }

    @Nonnull
    public LogContext result(final LogLevel level, final Throwable throwable, final String description, final Object... args) {
        return result(level, throwable, String.format(description, args));
    }

    @Nullable
    public Throwable throwable() {
        return throwable;
    }

    public LogContext throwable(@Nonnull final Throwable newThrowable) {
        if (throwable != null) {
            if (suppressedThrowables == null) {
                suppressedThrowables = new ArrayList<>(1);
            }

            suppressedThrowables.add(throwable);
        }

        this.throwable = newThrowable;
        return this;
    }

    @Nullable
    public ArrayList<Throwable> suppressedThrowables() {
        return suppressedThrowables;
    }

    @Nonnull
    public LogContext markTime() {
        this.timeMark = currentTimeMillis();
        return this;
    }

    @Nonnull
    public String trace() {
        if (trace != null) {
            return trace.toString();
        } else {
            return "";
        }
    }

    @Nonnull
    public LogContext trace(@Nonnull final String msg) {
        return trace(TraceFlag.NONE, msg);
    }

    @Nonnull
    public LogContext trace(@Nonnull final String msg, @Nonnull final Object... args) {
        return trace(TraceFlag.NONE, String.format(msg, args));
    }

    @Nonnull
    public LogContext trace(@Nonnull final TraceFlag flag, @Nonnull final String msg) {
        if (trace == null) {
            trace = new StringBuilder(128);
        } else {
            trace.append("; ");
        }

        switch (flag) {
            case NONE:
                trace.append(msg);
                return this;
            case MARK_TIME:
                trace.append(msg);
                markTime();
                return this;
            case WRITE_TIME:
            case WRITE_AND_MARK_TIME:
                trace.append(msg)
                        .append(" [")
                        .append(currentTimeMillis() - timeMark)
                        .append(" ms]");

                if (flag == TraceFlag.WRITE_AND_MARK_TIME) {
                    markTime();
                }

                return this;
            default:
                throw new RuntimeException("Internal error, unknown flag: " + flag);
        }
    }

    @Nonnull
    public LogContext trace(@Nonnull final TraceFlag flag, @Nonnull final String msg, @Nonnull final Object... args) {
        return trace(flag, String.format(msg, args));
    }

    @Nonnull
    public LogContext pushMDC(@Nonnull final String key, @Nullable final String value) {
        if (mdcStack == null) {
            mdcStack = new LinkedList<>();
        }

        final String old = MDC.get(key);
        mdcStack.push(new MDCEntry(key, old));
        MDC.put(key, value);

        return this;
    }

    protected LogContext clearMDC() {
        if (mdcStack != null) {
            while (!mdcStack.isEmpty()) {
                final MDCEntry entry = mdcStack.pop();

                MDC.put(entry.getKey(), entry.getValue());
            }
        }

        return this;
    }

    @Nullable
    protected String oldThreadName() {
        return oldThreadName;
    }

    protected void oldThreadName(@Nonnull final String oldThreadName) {
        if (this.oldThreadName == null) {
            this.oldThreadName = oldThreadName;
        }
    }

    @Nonnull
    public LogContext threadName(@Nonnull final String newName) {
        oldThreadName(Thread.currentThread().getName());
        Thread.currentThread().setName(newName);
        return this;
    }

    @Nonnull
    protected LogContext recoverThreadName() {
        if (oldThreadName != null) {
            Thread.currentThread().setName(oldThreadName);
        }

        return this;
    }

    @Override
    public void close() {
        SmartLog.finish();
    }

    /**
     * MDC entry contains key-value pair
     */
    private static final class MDCEntry {
        private final String key;
        private final String value;

        private MDCEntry(@Nonnull final String key, @Nullable final String value) {
            this.key = key;
            this.value = value;
        }

        @Nonnull
        public String getKey() {
            return key;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }
}