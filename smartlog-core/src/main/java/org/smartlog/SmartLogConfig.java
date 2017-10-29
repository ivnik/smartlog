package org.smartlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartlog.format.Format;
import org.smartlog.format.SimpleTextFormat;

import java.util.function.Consumer;

/**
 * todo -
 * 1) make class thread safe
 * 2) load settings from smartlog.properties using ClassLoader.getInputStream
 * 3) add method freeze (any changes - throw exception)
 * 4) fluent setters / builder
 */
public class SmartLogConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartLogConfig.class);

    private static SmartLogConfig config = new SmartLogConfig();

    private Format defaultFormat = new SimpleTextFormat("${title} - [${result}], trace: [${trace}] [${time} ms]");

    // todo - remove?
    private Consumer<String> internalErrorHandler = FAIL_FAST;

    private boolean replaceCrLf = true;

    public static SmartLogConfig getConfig() {
        return config;
    }

    public static void setConfig(final SmartLogConfig config) {
        SmartLogConfig.config = config;
    }

    public Format getDefaultFormat() {
        return defaultFormat;
    }

    public void setDefaultFormat(final Format defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public Consumer<String> getInternalErrorHandler() {
        return internalErrorHandler;
    }

    public void setInternalErrorHandler(final Consumer<String> internalErrorHandler) {
        this.internalErrorHandler = internalErrorHandler;
    }

    public boolean isReplaceCrLf() {
        return replaceCrLf;
    }

    public void setReplaceCrLf(final boolean replaceCrLf) {
        this.replaceCrLf = replaceCrLf;
    }

    public static final Consumer<String> FAIL_FAST = s -> {
        throw new RuntimeException(s);
    };

    public static final Consumer<String> LOG_ERROR = LOGGER::error;
}