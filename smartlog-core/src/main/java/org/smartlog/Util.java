package org.smartlog;

import javax.annotation.Nonnull;

public final class Util {
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

    @Nonnull
    public static Class findRootEnclosingClass(@Nonnull final Class clazz) {
        Class curr = clazz;
        while (true) {
            Class declaringClass = curr.getEnclosingClass();
            if (declaringClass != null) {
                curr = declaringClass;
            } else {
                return curr;
            }
        }
    }
}
