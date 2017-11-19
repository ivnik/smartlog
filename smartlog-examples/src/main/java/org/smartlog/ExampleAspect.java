package org.smartlog;

import org.smartlog.aop.Loggable;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

import static org.smartlog.TraceFlag.MARK_TIME;
import static org.smartlog.TraceFlag.WRITE_TIME;

public class ExampleAspect implements LoggableCallback {
    private static final Output SL_OUTPUT = Slf4JOutput.create()
            .withLoggerFor(ExampleAspect.class)
//            .withFormat(new SimpleTextFormat("${title}, result: [${result}], var=${var}, trace: [${trace}] [${time} ms]"))
            .build();

    @Loggable
    public static int example1() {
        return 42;
    }

    @Loggable
    public static void example2() {
        throw new RuntimeException("example uncaught exception");
    }

    @Loggable
    public static void example3() {
        SmartLog.title("Custom title");

        SmartLog.trace(MARK_TIME, "make request to...");
        // request remote server
        SmartLog.trace(WRITE_TIME, "got result %d", 42);

        SmartLog.trace("try parse");
        // parse
        SmartLog.trace("ok");

        SmartLog.result("custom result");
    }

    @Loggable
    public static void example4() {
        SmartLog.title("it's example4");

        SmartLog.throwable(new RuntimeException("exception1"));
        SmartLog.throwable(new RuntimeException("exception2"));

        SmartLog.result(LogLevel.WARN, new RuntimeException("exception3"), "test error");
    }

    @Loggable
    public static String example5(final int value) {
        SmartLog.title("it's example5, arg=%d", value);
        return String.valueOf(value);
    }

    @Loggable(defaultLevel = LogLevel.DEBUG)
    public static void example5() {
    }

    @Override
    public void beforeLoggable() {
        SmartLog.output(SL_OUTPUT);
    }

    public static void main(final String[] args) {
        ExampleAspect.example1();

        try {
            ExampleAspect.example2();
        } catch (RuntimeException ignore) {
        }

        ExampleAspect.example3();
        ExampleAspect.example4();
        ExampleAspect.example5(5);
        ExampleAspect.example5();
    }
}
