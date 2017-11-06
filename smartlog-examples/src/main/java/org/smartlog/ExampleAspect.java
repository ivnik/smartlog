package org.smartlog;

import org.smartlog.aop.Loggable;
import org.smartlog.format.SimpleTextFormat;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

/**
 * todo - make mock Output and add checks
 */
public class ExampleAspect implements LoggableCallback {
    private static final Output SL_OUTPUT = Slf4JOutput.create()
            .withLoggerFor(ExampleAspect.class)
            .withFormat(new SimpleTextFormat("${title}, result: [${result}], var=${var}, trace: [${trace}] [${time} ms]"))
            .build();

    @Loggable
    public static void example1() {
        SmartLog.trace("test");
        SmartLog.trace("hello %s", "alice");

        SmartLog.result("OK");
    }

    @Loggable
    public static void example2MultipleExceptions() {
        SmartLog.title("example2");

        SmartLog.throwable(new RuntimeException("exception1"));
        SmartLog.throwable(new RuntimeException("exception2"));

        SmartLog.result(LogLevel.WARN, new RuntimeException("exception3"), "test error");
    }

    @Loggable
    public static String example3ResultFromReturnValue(final int value) {
        SmartLog.title("example3(%d)", value);
        return String.valueOf(value);
    }

    @Loggable(defaultLevel = LogLevel.DEBUG)
    public static void example4() {
    }

    @Override
    public void beforeLoggable() {
        SmartLog.output(SL_OUTPUT);
    }

    public static void main(final String[] args) {
        ExampleAspect.example1();
        ExampleAspect.example2MultipleExceptions();
        ExampleAspect.example3ResultFromReturnValue(5);
        ExampleAspect.example4();
    }
}
