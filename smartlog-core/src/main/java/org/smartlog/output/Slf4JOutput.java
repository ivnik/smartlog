package org.smartlog.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartlog.LogContext;
import org.smartlog.LogLevel;
import org.smartlog.SmartLogConfig;
import org.smartlog.Util;
import org.smartlog.format.Format;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 *
 */
public class Slf4JOutput implements Output {
    @Nonnull
    private final Logger logger;

    private final Format format;

    private final Boolean replaceCrLf;

    public Slf4JOutput(final Builder builder) {
        if (builder.logger == null) {
            throw new RuntimeException("Logger is absent");
        }

        this.logger = builder.logger;
        this.format = builder.format;
        this.replaceCrLf = builder.replaceCrLf;
    }

    public static Builder create() {
        return new Builder();
    }

    @Override
    public void write(final LogContext log) {
        final LogLevel level = log.level();
        final Throwable throwable = log.throwable();
        final ArrayList<Throwable> suppressedThrowables = log.suppressedThrowables();

        if (throwable != null && suppressedThrowables != null) {
            for (Throwable t : suppressedThrowables) {
                // don't let it supress itself
                if (throwable != t) {
                    throwable.addSuppressed(t);
                }
            }
        }

        switch (level != null ? level : LogLevel.INFO) {
            case DEBUG:
                if (logger.isDebugEnabled()) {
                    final String message = format(log);

                    if (throwable != null) {
                        logger.debug(message, throwable);
                    } else {
                        logger.debug(message);
                    }
                }
                break;
            case INFO:
                if (logger.isInfoEnabled()) {
                    final String message = format(log);

                    if (throwable != null) {
                        logger.info(message, throwable);
                    } else {
                        logger.info(message);
                    }
                }
                break;
            case WARN:
                if (logger.isWarnEnabled()) {
                    final String message = format(log);

                    if (throwable != null) {
                        logger.warn(message, throwable);
                    } else {
                        logger.warn(message);
                    }
                }
                break;
            case ERROR:
            default:
                if (logger.isErrorEnabled()) {
                    final String message = format(log);

                    if (throwable != null) {
                        logger.error(message, throwable);
                    } else {
                        logger.error(message);
                    }
                }
        }
    }

    @Nonnull
    public Logger getLogger() {
        return logger;
    }

    @Nullable
    public Format getFormat() {
        return format;
    }

    @Nullable
    public Boolean getReplaceCrLf() {
        return replaceCrLf;
    }

    private String format(final LogContext log) {
        final Format fmt = selectFormat(log);
        final String rawMessage = fmt.format(log);

        final boolean replace = replaceCrLf == null ? SmartLogConfig.getConfig().isReplaceCrLf() : replaceCrLf;

        return replace ? Util.stripCrLf(rawMessage) : rawMessage;
    }

    @Nonnull
    private Format selectFormat(final LogContext log) {
        final Format currFormat = log.format();
        if (currFormat != null) {
            return currFormat;
        }

        if (this.format != null) {
            return this.format;
        }

        return SmartLogConfig.getConfig().getDefaultFormat();
    }

    public static class Builder {
        private Logger logger;
        private Format format;

        private Boolean replaceCrLf;

        protected Builder() {
        }

        public Builder withLogger(final Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder withLoggerFor(final Class clazz) {
            this.logger = LoggerFactory.getLogger(clazz);
            return this;
        }

        public Builder withLoggerFor(final String name) {
            this.logger = LoggerFactory.getLogger(name);
            return this;
        }

        public Builder withLoggerFor(final Class clazz, final String name) {
            this.logger = LoggerFactory.getLogger(clazz.getName() + "." + name);
            return this;
        }

        public Builder withFormat(final Format format) {
            this.format = format;
            return this;
        }

        public Builder replaceCrLf() {
            this.replaceCrLf = true;
            return this;
        }

        public Builder keepCrLf() {
            this.replaceCrLf = false;
            return this;
        }

        public Slf4JOutput build() {
            return new Slf4JOutput(this);
        }
    }
}