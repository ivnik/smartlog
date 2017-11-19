package org.smartlog;

import org.smartlog.format.SimpleTextFormat;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

/**
 *
 */
public class ExampleNoAspect {
    private static final Output SL_OUTPUT = Slf4JOutput.create()
            .withLoggerFor(ExampleNoAspect.class)
            .withFormat(new SimpleTextFormat("${title}, result: [${result}], var=${var}, trace: [${trace}] [${time} ms]"))
            .build();

    public static void example1() {
        SmartLog.start(SL_OUTPUT)
                .title("main");

        try {
            SmartLog.trace("test");
            SmartLog.trace("hello %s", "alice");

            SmartLog.attach("var", 5);

            SmartLog.result("OK");

        } finally {
            SmartLog.finish();
        }
    }

    public static void example2() {
        try (LogContext log = SmartLog.start(SL_OUTPUT).title("main")) {
            log.trace("test");
            log.trace("hello %s", "alice");

            log.attach("var", 5);

            log.result("OK");
        }
    }

    public static void main(final String[] args) {
        ExampleNoAspect.example1();
        ExampleNoAspect.example2();
    }
}