package org.smartlog;

import org.junit.Test;
import org.smartlog.aop.Loggable;
import org.smartlog.aop.LoggableCallback;
import org.smartlog.format.SimpleTextFormat;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

/**
 * todo - make mock Output and add checks
 */
public class ExampleTest implements LoggableCallback {
    private static final Output SL_OUTPUT = Slf4JOutput.create()
            .withLoggerFor(ExampleTest.class)
            .withFormat(new SimpleTextFormat("${title}, result: [${result}], var=${var}, trace: [${trace}] [${time} ms]"))
            .build();

    @Override
    public void beforeLoggable() {
        SmartLog.output(SL_OUTPUT);
    }

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


    @Test
    public void test() {
        ExampleTest.example1();
        ExampleTest.example2MultipleExceptions();
        ExampleTest.example3ResultFromReturnValue(5);
        ExampleTest.example4();
    }
}
