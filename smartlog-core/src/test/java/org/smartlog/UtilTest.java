package org.smartlog;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilTest {
    @Test
    public void stripCrLf() throws Exception {
        assertThat(Util.stripCrLf("")).isEqualTo("");
        assertThat(Util.stripCrLf("\n\rtest\r\n")).isEqualTo("\\n\\rtest\\r\\n");
    }

    @Test
    public void findRootEnclosingClass() throws Exception {
        assertThat(Util.findRootEnclosingClass(UtilTest.class)).isSameAs(UtilTest.class);
    }

    @Test
    public void findRootEnclosingClassForAnonymousClass() throws Exception {
        Class<? extends Object> internalClass = new Object() {
        }.getClass();

        assertThat(Util.findRootEnclosingClass(internalClass)).isSameAs(UtilTest.class);
    }

    @Test
    public void findRootEnclosingClassForInternalClass() throws Exception {
        assertThat(Util.findRootEnclosingClass(InternalClass.class)).isSameAs(UtilTest.class);
    }

    private class InternalClass {
    }
}