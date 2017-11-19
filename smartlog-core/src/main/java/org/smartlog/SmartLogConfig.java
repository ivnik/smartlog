package org.smartlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartlog.format.Format;
import org.smartlog.format.SimpleTextFormat;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * todo -
 * 1) make class thread safe
 * 2) load settings from smartlog.properties using ClassLoader.getInputStream
 * 3) add method freeze? (protect from changes - throw exception)
 * 4) fluent setters / builder
 */
public class SmartLogConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmartLogConfig.class);

    private static SmartLogConfig config = new SmartLogConfig();

    private Format defaultFormat = new SimpleTextFormat("${title} - [${result}], trace: [${trace}] [${time} ms]");

    private boolean replaceCrLf = true;

    private Function<Class, Output> defaultOutputResolver = (clazz) -> Slf4JOutput.create()
            .withLoggerFor(clazz)
            .build();

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

    public boolean isReplaceCrLf() {
        return replaceCrLf;
    }

    public void setReplaceCrLf(final boolean replaceCrLf) {
        this.replaceCrLf = replaceCrLf;
    }

    @Nonnull
    public Output getDefaultOutput(final Class clazz) {
        // todo - add cache class -> output
        return defaultOutputResolver.apply(clazz);
    }

    public void setDefaultOutputResolver(final Function<Class, Output> defaultOutputResolver) {
        this.defaultOutputResolver = defaultOutputResolver;
    }
}