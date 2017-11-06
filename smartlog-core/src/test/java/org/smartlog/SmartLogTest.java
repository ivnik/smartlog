package org.smartlog;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.smartlog.format.SimpleTextFormat;
import org.smartlog.output.Output;
import org.smartlog.output.Slf4JOutput;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.smartlog.TraceFlag.WRITE_TIME;

public class SmartLogTest {
    private final Logger logger = mock(Logger.class);

    private final Output output = Slf4JOutput.create()
            .withLogger(logger)
            .build();

    @Before
    public void setup() {
        reset(logger);

        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isWarnEnabled()).thenReturn(true);
        when(logger.isErrorEnabled()).thenReturn(true);
    }


    @Test
    public void complexTest1() throws Exception {
        SmartLog.start(output)
                .level(LogLevel.DEBUG)
                .title("test-%s", "title")
                .format(new SimpleTextFormat("${title} - ${result}, var=${var}, trace: [${trace}] [${time} ms]"))
                .trace("trace1")
                .trace("trace2")
                .trace(WRITE_TIME, "trace3")
                .trace("trace%d", 4)
                .attach("var", "val")
                .result("test-result");

        SmartLog.finish();

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(logger).debug(msgCaptor.capture());

        Assertions.assertThat(msgCaptor.getValue())
                .matches("test-title - test-result, var=val, trace: \\[trace1; trace2; trace3 \\[\\d+ ms\\]; trace4\\] \\[\\d+ ms\\]");
    }

    @Test
    public void complexTest2() throws Exception {
        SmartLog.start(output);

        SmartLog.format(new SimpleTextFormat("${title} - ${result}, var=${var}, trace: [${trace}] [${time} ms]"));
        SmartLog.level(LogLevel.DEBUG);
        SmartLog.title("test-title");
        SmartLog.attach("var", "val");
        SmartLog.trace("trace1");
        SmartLog.trace("trace2");
        SmartLog.trace(TraceFlag.WRITE_TIME, "trace3");
        SmartLog.trace("trace%d", 4);
        SmartLog.result("test-result");

        Thread.sleep(5);

        SmartLog.finish();

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(logger).debug(msgCaptor.capture());

        Assertions.assertThat(msgCaptor.getValue())
                .matches("test-title - test-result, var=val, trace: \\[trace1; trace2; trace3 \\[\\d+ ms\\]; trace4\\] \\[\\d+ ms\\]");
    }

    @Test
    public void testWithException() throws Exception {
        final Exception e1 = new Exception("e1");
        final Exception e2 = new Exception("e2");
        final Exception e3 = new Exception("e3");

        SmartLog.start(output)
                .throwable(e1)
                .throwable(e2)
                .throwable(e3);

        SmartLog.finish();

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        Mockito.verify(logger).info(anyString(), exceptionCaptor.capture());

        assertThat(exceptionCaptor.getValue()).hasSuppressedException(e1);
        assertThat(exceptionCaptor.getValue()).hasSuppressedException(e2);
        assertThat(exceptionCaptor.getValue()).isEqualTo(e3);
    }

    @Test
    public void testCallback() throws Exception {
        final LoggableCallback loggableCallback = mock(LoggableCallback.class);
        SmartLog.start(output, loggableCallback);

        Mockito.verify(loggableCallback).beforeLoggable();

        SmartLog.finish();

        Mockito.verify(loggableCallback).afterLoggable();
    }

    @Test
    public void testMdc() throws Exception {
        MDC.put("mdc-var1", "mdc-oldval");

        SmartLog.start(output)
                .pushMDC("mdc-var1", "mdc-val1")
                .pushMDC("mdc-var2", "mdc-val2");

        assertThat(MDC.get("mdc-var1")).isEqualTo("mdc-val1");
        assertThat(MDC.get("mdc-var2")).isEqualTo("mdc-val2");

        SmartLog.finish();

        assertThat(MDC.get("mdc-var1")).isEqualTo("mdc-oldval");
        assertThat(MDC.get("mdc-var2")).isNull();
    }

    @Test
    public void testThreadName() throws Exception {
        final String oldName = Thread.currentThread().getName();

        SmartLog.start(output)
                .threadName("new-thread-name");

        assertThat(Thread.currentThread().getName()).isEqualTo("new-thread-name");

        SmartLog.finish();

        assertThat(Thread.currentThread().getName()).isEqualTo(oldName);
    }
}