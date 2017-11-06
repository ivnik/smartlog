package org.smartlog;

import javax.annotation.Nonnull;

public class Util {
    @Nonnull
    public static String stripCrLf(@Nonnull final String text) {
        final char[] src = text.toCharArray();
        final StringBuilder dest = new StringBuilder(src.length + 10);

        for (final char ch : src) {
            switch (ch) {
                case 10:
                    dest.append("\\n");
                    break;
                case 13:
                    dest.append("\\r");
                    break;
                default:
                    dest.append(ch);
            }
        }

        return dest.toString();
    }
}
