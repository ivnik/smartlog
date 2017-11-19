package org.smartlog.aop;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.smartlog.LogContext;
import org.smartlog.LogLevel;
import org.smartlog.SmartLog;
import org.smartlog.SmartLogConfig;
import org.smartlog.output.Output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

public class LogAspectTest {
    private static final Output output = mock(Output.class);

    @BeforeClass
    public static void setupClass() {
        SmartLogConfig.getConfig().setDefaultOutputResolver(clazz -> output);
    }

    @Before
    public void setup() {
        reset(output);
    }

    @Loggable
    public static void log1UseMethodNameAsTitle() {
    }

    @Loggable(defaultLevel = LogLevel.DEBUG)
    public static void log2DefaultLogLevel() {
        SmartLog.title("log2");
    }

    @Loggable
    public static void log3UncaughtException() {
        throw new RuntimeException("test");
    }

    @Loggable
    public static int log4UseReturnedValueAsResult() {
        return 42;
    }

    @Loggable
    public static int log5() {
        SmartLog.result("OK");
        return 42;
    }

    @Test
    public void test1() {
        log1UseMethodNameAsTitle();

        final ArgumentCaptor<LogContext> ctxCaptor = ArgumentCaptor.forClass(LogContext.class);
        verify(output).write(ctxCaptor.capture());

        final LogContext ctx = ctxCaptor.getValue();
        assertThat(ctx.title()).isEqualTo("log1UseMethodNameAsTitle");
        assertThat(ctx.level()).isEqualTo(LogLevel.INFO);
        assertThat(ctx.result()).isNull();
    }

    @Test
    public void test2() {
        log2DefaultLogLevel();

        final ArgumentCaptor<LogContext> ctxCaptor = ArgumentCaptor.forClass(LogContext.class);
        verify(output).write(ctxCaptor.capture());

        final LogContext ctx = ctxCaptor.getValue();
        assertThat(ctx.level()).isEqualTo(LogLevel.DEBUG);
        assertThat(ctx.title()).isEqualTo("log2");
    }

    @Test
    public void test3() {
        try {
            log3UncaughtException();
            Assert.fail("no expected exception");
        } catch (Exception e) {
            // it's ok
        }

        final ArgumentCaptor<LogContext> ctxCaptor = ArgumentCaptor.forClass(LogContext.class);
        verify(output).write(ctxCaptor.capture());

        final LogContext ctx = ctxCaptor.getValue();
        assertThat(ctx.throwable())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    public void test4() {
        log4UseReturnedValueAsResult();

        final ArgumentCaptor<LogContext> ctxCaptor = ArgumentCaptor.forClass(LogContext.class);
        verify(output).write(ctxCaptor.capture());

        final LogContext ctx = ctxCaptor.getValue();
        assertThat(ctx.result()).isEqualTo(42);
    }

    @Test
    public void test5() {
        log5();

        final ArgumentCaptor<LogContext> ctxCaptor = ArgumentCaptor.forClass(LogContext.class);
        verify(output).write(ctxCaptor.capture());

        final LogContext ctx = ctxCaptor.getValue();
        assertThat(ctx.result()).isEqualTo("OK");
    }
}