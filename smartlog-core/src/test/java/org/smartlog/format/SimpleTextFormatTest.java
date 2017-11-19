package org.smartlog.format;


import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.smartlog.LogContext;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class SimpleTextFormatTest {
    @Test
    public void testTitle() {
        final LogContext ctx = Mockito.mock(LogContext.class);
        when(ctx.title()).thenReturn("test-title");

        String result = new SimpleTextFormat("[${title}]").format(ctx);

        Assertions.assertThat(result).isEqualTo("[test-title]");
    }

    @Test
    public void testResult() {
        final LogContext ctx = Mockito.mock(LogContext.class);
        when(ctx.result()).thenReturn("test-result");

        String result = new SimpleTextFormat("[${result}]").format(ctx);

        Assertions.assertThat(result).isEqualTo("[test-result]");
    }

    @Test
    public void testResultNull() {
        final LogContext ctx = Mockito.mock(LogContext.class);
        when(ctx.result()).thenReturn(null);

        String result = new SimpleTextFormat("[${result}]").format(ctx);

        Assertions.assertThat(result).isEqualTo("[]");
    }

    @Test
    public void testResultToString() {
        final LogContext ctx = Mockito.mock(LogContext.class);
        when(ctx.result()).thenReturn(new Object() {
            @Override
            public String toString() {
                return "to-string";
            }
        });

        String result = new SimpleTextFormat("[${result}]").format(ctx);

        Assertions.assertThat(result).isEqualTo("[to-string]");
    }

    @Test
    public void testTrace() {
        final LogContext ctx = Mockito.mock(LogContext.class);
        when(ctx.trace()).thenReturn("test-trace");

        String result = new SimpleTextFormat("[${trace}]").format(ctx);

        Assertions.assertThat(result).isEqualTo("[test-trace]");
    }

    @Test
    public void testTime() {
        final LogContext ctx = Mockito.mock(LogContext.class);
        when(ctx.startTime()).thenReturn(100L);
        when(ctx.endTime()).thenReturn(300L);

        String result = new SimpleTextFormat("[${time}]").format(ctx);

        Assertions.assertThat(result).isEqualTo("[200]");
    }

    @Test
    public void testAttrs() {
        final LogContext ctx = Mockito.mock(LogContext.class);
        when(ctx.getAttr(eq("attr1"))).thenReturn("value1");
        when(ctx.getAttr(eq("attr2"))).thenReturn(null);
        when(ctx.getAttr(eq("attr3"))).thenReturn(new Object() {
            @Override
            public String toString() {
                return "to-string";
            }
        });

        String result = new SimpleTextFormat("[${attr1}][${attr2}][${attr3}]").format(ctx);

        Assertions.assertThat(result).isEqualTo("[value1][][to-string]");
    }
}