package org.smartlog;

import org.smartlog.format.SimpleTextFormat;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

/**
 *
 */
public class Example {
    private static final Output SL_OUTPUT = Slf4JOutput.create()
        .withLoggerFor(Example.class)
        .withFormat(new SimpleTextFormat("${title}, result: [${result}], var=${var}, trace: [${trace}] [${time} ms]"))
        .build();

    public static void example() {
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

    public static void main(final String[] args) {
        Example.example();
    }
}