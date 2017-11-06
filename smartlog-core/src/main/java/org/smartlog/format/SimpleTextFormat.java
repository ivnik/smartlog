package org.smartlog.format;

import org.smartlog.LogContext;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo - write tests
 */
public class SimpleTextFormat implements Format {
    private static final Pattern DEFAULT_VARIABLE_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    private static final Map<String, Token> STD_ATTRS;

    static {
        STD_ATTRS = new HashMap<>();
        STD_ATTRS.put("title", (log, builder) -> builder.append(log.title()));
        STD_ATTRS.put("result", (log, builder) -> {
            if (log.result() != null) {
                builder.append(log.result());
            }
        });
        STD_ATTRS.put("trace", (log, builder) -> builder.append(log.trace()));
        STD_ATTRS.put("time", (log, builder) -> builder.append(log.endTime() - log.startTime()));
    }

    @Nonnull
    private final String format;
    @Nonnull
    private final ArrayList<Token> tokens;

    public SimpleTextFormat(@Nonnull final String format) {
        this(format, DEFAULT_VARIABLE_PATTERN);
    }

    public SimpleTextFormat(@Nonnull final String format, @Nonnull final Pattern variablePattern) {
        this.format = format;

        final Matcher matcher = variablePattern.matcher(format);

        int pos = 0;
        tokens = new ArrayList<>();
        while (matcher.find(pos)) {
            int start = matcher.start();
            if (start > pos) {
                tokens.add(new TextToken(format.substring(pos, start)));
            }

            final String name = matcher.group(1);
            final Token stdAttr = STD_ATTRS.get(name);
            if (stdAttr != null) {
                tokens.add(stdAttr);
            } else {
                tokens.add(new AttributeToken(name));
            }

            pos = matcher.end();
        }

        if (pos < format.length()) {
            tokens.add(new TextToken(format.substring(pos)));
        }

        tokens.trimToSize();
    }

    @Override
    public String format(@Nonnull final LogContext log) {
        final StringBuilder builder = new StringBuilder();
        for (final Token token : tokens) {
            token.append(log, builder);
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return format;
    }

    @FunctionalInterface
    interface Token {
        void append(LogContext log, StringBuilder builder);
    }

    class TextToken implements Token {
        private final String text;

        TextToken(final String text) {
            this.text = text;
        }

        @Override
        public void append(final LogContext log, final StringBuilder builder) {
            builder.append(text);
        }
    }

    class AttributeToken implements Token {
        private final String name;

        AttributeToken(final String name) {
            this.name = name;
        }

        @Override
        public void append(final LogContext log, final StringBuilder builder) {
            final Object value = log.getAttr(name);
            if (value != null) {
                builder.append(value);
            }
        }
    }
}