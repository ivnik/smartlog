package org.smartlog.output;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.smartlog.LogContext;
import org.smartlog.LogLevel;
import org.smartlog.SmartLogConfig;
import org.smartlog.format.SimpleTextFormat;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class Slf4JOutputTest {
    private final LogContext ctx = Mockito.mock(LogContext.class);
    private final Logger logger = Mockito.mock(Logger.class);

    private final Output output = Slf4JOutput.create()
            .withLogger(logger)
            .build();

    @BeforeClass
    public static void setupClass() {
        SmartLogConfig
                .getConfig()
                .setDefaultFormat(new SimpleTextFormat("global-format"));
    }

    @Before
    public void setup() {
        reset(logger);
        reset(ctx);
        when(ctx.output()).thenReturn(output);
    }

    @Test
    public void doNotWriteDebugLogIfDisabled() throws Exception {
        when(logger.isDebugEnabled()).thenReturn(false);
        when(ctx.level()).thenReturn(LogLevel.DEBUG);

        output.write(ctx);

        Mockito.verify(logger, never()).debug(anyString());
        Mockito.verify(logger).isDebugEnabled();
    }

    @Test
    public void doNotWriteInfoLogIfDisabled() throws Exception {
        when(logger.isInfoEnabled()).thenReturn(false);
        when(ctx.level()).thenReturn(LogLevel.INFO);

        output.write(ctx);

        Mockito.verify(logger, never()).info(anyString());
        Mockito.verify(logger).isInfoEnabled();
    }

    @Test
    public void doNotWriteWarnLogIfDisabled() throws Exception {
        when(logger.isWarnEnabled()).thenReturn(false);
        when(ctx.level()).thenReturn(LogLevel.WARN);

        output.write(ctx);

        Mockito.verify(logger, never()).warn(anyString());
        Mockito.verify(logger).isWarnEnabled();
    }

    @Test
    public void doNotWriteErrorLogIfDisabled() throws Exception {
        when(logger.isErrorEnabled()).thenReturn(false);
        when(ctx.level()).thenReturn(LogLevel.ERROR);

        output.write(ctx);

        Mockito.verify(logger, never()).error(anyString());
        Mockito.verify(logger).isErrorEnabled();
    }

    @Test
    public void testLogDebug() throws Exception {
        when(logger.isDebugEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.DEBUG);

        output.write(ctx);

        Mockito.verify(logger).debug(anyString());
    }

    @Test
    public void testLogInfo() throws Exception {
        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.INFO);

        output.write(ctx);

        Mockito.verify(logger).info(anyString());
    }

    @Test
    public void testLogWarn() throws Exception {
        when(logger.isWarnEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.WARN);

        output.write(ctx);

        Mockito.verify(logger).warn(anyString());
    }

    @Test
    public void testLogError() throws Exception {
        when(logger.isErrorEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.ERROR);

        output.write(ctx);

        Mockito.verify(logger).error(anyString());
    }

    @Test
    public void testLogInfoIfLogLevelNull() throws Exception {
        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(null);

        output.write(ctx);

        Mockito.verify(logger).info(anyString());
    }


    @Test
    public void testLogDebugWithThrowable() throws Exception {
        final Exception exception = new Exception();

        when(logger.isDebugEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.DEBUG);
        when(ctx.throwable()).thenReturn(exception);

        output.write(ctx);

        Mockito.verify(logger).debug(anyString(), eq(exception));
    }

    @Test
    public void testLogInfogWithThrowable() throws Exception {
        final Exception exception = new Exception();

        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.INFO);
        when(ctx.throwable()).thenReturn(exception);

        output.write(ctx);

        Mockito.verify(logger).info(anyString(), eq(exception));
    }

    @Test
    public void testLogWarngWithThrowable() throws Exception {
        final Exception exception = new Exception();

        when(logger.isWarnEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.WARN);
        when(ctx.throwable()).thenReturn(exception);

        output.write(ctx);

        Mockito.verify(logger).warn(anyString(), eq(exception));
    }

    @Test
    public void testLogErrorgWithThrowable() throws Exception {
        final Exception exception = new Exception();

        when(logger.isErrorEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.ERROR);
        when(ctx.throwable()).thenReturn(exception);

        output.write(ctx);

        Mockito.verify(logger).error(anyString(), eq(exception));
    }

    @Test
    public void testLogContextFormat() throws Exception {
        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.INFO);
        when(ctx.format()).thenReturn(new SimpleTextFormat("context-format"));

        output.write(ctx);

        Mockito.verify(logger).info(eq("context-format"));
    }

    @Test
    public void testUseOutputFormatIfNoContextFormatPresent() throws Exception {
        final Output output = Slf4JOutput.create()
                .withLogger(logger)
                .withFormat(new SimpleTextFormat("output-format"))
                .build();

        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.INFO);
        when(ctx.format()).thenReturn(null);
        when(ctx.output()).thenReturn(output);

        output.write(ctx);

        Mockito.verify(logger).info(eq("output-format"));
    }

    @Test
    public void testUseGlobalFormatIfNoContextOrOutputFormatsPresent() throws Exception {
        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.INFO);
        when(ctx.format()).thenReturn(null);

        output.write(ctx);

        Mockito.verify(logger).info(eq("global-format"));
    }

    @Test
    public void testStripCrLf() throws Exception {
        final Output output = Slf4JOutput.create()
                .withLogger(logger)
                .withFormat(new SimpleTextFormat("output-format:\n\r${title}\n\r"))
                .replaceCrLf()
                .build();

        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.INFO);
        when(ctx.output()).thenReturn(output);
        when(ctx.title()).thenReturn("test\ntitle");

        output.write(ctx);

        Mockito.verify(logger).info(eq("output-format:\\n\\rtest\\ntitle\\n\\r"));
    }

    @Test
    public void testKeepCrLf() throws Exception {
        final Output output = Slf4JOutput.create()
                .withLogger(logger)
                .withFormat(new SimpleTextFormat("output-format:\n\r${title}\n\r"))
                .keepCrLf()
                .build();

        when(logger.isInfoEnabled()).thenReturn(true);
        when(ctx.level()).thenReturn(LogLevel.INFO);
        when(ctx.output()).thenReturn(output);
        when(ctx.title()).thenReturn("test\ntitle");

        output.write(ctx);

        Mockito.verify(logger).info(eq("output-format:\n\rtest\ntitle\n\r"));
    }
}