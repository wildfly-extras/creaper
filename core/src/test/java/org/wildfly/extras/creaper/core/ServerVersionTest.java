package org.wildfly.extras.creaper.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ServerVersionTest {
    @Test
    public void referenceEquality() {
        assertSame(ServerVersion.VERSION_20_0_0, ServerVersion.from(20, 0, 0));
        assertSame(ServerVersion.VERSION_21_0_0, ServerVersion.from(21, 0, 0));
        assertSame(ServerVersion.VERSION_22_0_0, ServerVersion.from(22, 0, 0));
        assertSame(ServerVersion.VERSION_23_0_0, ServerVersion.from(23, 0, 0));
        assertSame(ServerVersion.VERSION_24_0_0, ServerVersion.from(24, 0, 0));
        assertSame(ServerVersion.VERSION_25_0_0, ServerVersion.from(25, 0, 0));
        assertSame(ServerVersion.VERSION_26_0_0, ServerVersion.from(26, 0, 0));
        assertSame(ServerVersion.VERSION_27_0_0, ServerVersion.from(27, 0, 0));
        assertSame(ServerVersion.VERSION_28_0_0, ServerVersion.from(28, 0, 0));
        assertSame(ServerVersion.VERSION_29_0_0, ServerVersion.from(29, 0, 0));
        assertSame(ServerVersion.VERSION_30_0_0, ServerVersion.from(30, 0, 0));
        assertSame(ServerVersion.VERSION_31_0_0, ServerVersion.from(31, 0, 0));
        assertSame(ServerVersion.VERSION_32_0_0, ServerVersion.from(32, 0, 0));
        assertSame(ServerVersion.VERSION_33_0_0, ServerVersion.from(33, 0, 0));

        assertNotSame(ServerVersion.from(42, 42, 42), ServerVersion.from(42, 42, 42));
    }

    @Test
    public void equality() {
        assertSame(ServerVersion.VERSION_20_0_0, ServerVersion.from(20, 0, 0));
        assertSame(ServerVersion.VERSION_21_0_0, ServerVersion.from(21, 0, 0));
        assertSame(ServerVersion.VERSION_22_0_0, ServerVersion.from(22, 0, 0));
        assertSame(ServerVersion.VERSION_23_0_0, ServerVersion.from(23, 0, 0));
        assertSame(ServerVersion.VERSION_24_0_0, ServerVersion.from(24, 0, 0));
        assertSame(ServerVersion.VERSION_25_0_0, ServerVersion.from(25, 0, 0));
        assertSame(ServerVersion.VERSION_26_0_0, ServerVersion.from(26, 0, 0));
        assertSame(ServerVersion.VERSION_27_0_0, ServerVersion.from(27, 0, 0));
        assertSame(ServerVersion.VERSION_28_0_0, ServerVersion.from(28, 0, 0));
        assertSame(ServerVersion.VERSION_29_0_0, ServerVersion.from(29, 0, 0));
        assertSame(ServerVersion.VERSION_30_0_0, ServerVersion.from(30, 0, 0));
        assertSame(ServerVersion.VERSION_31_0_0, ServerVersion.from(31, 0, 0));
        assertSame(ServerVersion.VERSION_32_0_0, ServerVersion.from(32, 0, 0));
        assertSame(ServerVersion.VERSION_33_0_0, ServerVersion.from(33, 0, 0));

        assertEquals(ServerVersion.from(42, 42, 42), ServerVersion.from(42, 42, 42));
    }

    @Test
    public void equalTo() {
        assertTrue(ServerVersion.from(0, 0, 0).equalTo(ServerVersion.from(0, 0, 0)));
        assertTrue(ServerVersion.from(1, 0, 0).equalTo(ServerVersion.from(1, 0, 0)));
        assertTrue(ServerVersion.from(2, 0, 0).equalTo(ServerVersion.from(2, 0, 0)));

        assertFalse(ServerVersion.from(0, 0, 0).equalTo(ServerVersion.from(1, 0, 0)));
        assertFalse(ServerVersion.from(1, 0, 0).equalTo(ServerVersion.from(2, 0, 0)));
        assertFalse(ServerVersion.from(2, 0, 0).equalTo(ServerVersion.from(0, 0, 0)));
    }

    @Test
    public void lessThan() {
        assertTrue(ServerVersion.from(0, 0, 0).lessThan(ServerVersion.from(1, 0, 0)));
        assertTrue(ServerVersion.from(1, 0, 0).lessThan(ServerVersion.from(2, 0, 0)));
        assertTrue(ServerVersion.from(2, 0, 0).lessThan(ServerVersion.from(2, 1, 0)));

        assertFalse(ServerVersion.from(1, 0, 0).lessThan(ServerVersion.from(0, 0, 0)));
        assertFalse(ServerVersion.from(2, 0, 0).lessThan(ServerVersion.from(1, 0, 0)));
        assertFalse(ServerVersion.from(2, 1, 0).lessThan(ServerVersion.from(2, 0, 0)));
    }

    @Test
    public void lessThanOrEqualTo() {
        assertTrue(ServerVersion.from(0, 0, 0).lessThanOrEqualTo(ServerVersion.from(0, 0, 0)));
        assertTrue(ServerVersion.from(0, 0, 0).lessThanOrEqualTo(ServerVersion.from(1, 0, 0)));
        assertTrue(ServerVersion.from(1, 0, 0).lessThanOrEqualTo(ServerVersion.from(1, 0, 0)));
        assertTrue(ServerVersion.from(1, 0, 0).lessThanOrEqualTo(ServerVersion.from(2, 0, 0)));
        assertTrue(ServerVersion.from(2, 0, 0).lessThanOrEqualTo(ServerVersion.from(2, 0, 0)));
        assertTrue(ServerVersion.from(2, 0, 0).lessThanOrEqualTo(ServerVersion.from(2, 1, 0)));

        assertFalse(ServerVersion.from(1, 0, 0).lessThanOrEqualTo(ServerVersion.from(0, 0, 0)));
        assertFalse(ServerVersion.from(2, 0, 0).lessThanOrEqualTo(ServerVersion.from(1, 0, 0)));
        assertFalse(ServerVersion.from(2, 1, 0).lessThanOrEqualTo(ServerVersion.from(2, 0, 0)));
    }

    @Test
    public void greaterThan() {
        assertTrue(ServerVersion.from(1, 0, 0).greaterThan(ServerVersion.from(0, 0, 0)));
        assertTrue(ServerVersion.from(2, 0, 0).greaterThan(ServerVersion.from(1, 0, 0)));
        assertTrue(ServerVersion.from(2, 1, 0).greaterThan(ServerVersion.from(2, 0, 0)));

        assertFalse(ServerVersion.from(0, 0, 0).greaterThan(ServerVersion.from(1, 0, 0)));
        assertFalse(ServerVersion.from(1, 0, 0).greaterThan(ServerVersion.from(2, 0, 0)));
        assertFalse(ServerVersion.from(2, 0, 0).greaterThan(ServerVersion.from(2, 1, 0)));
    }

    @Test
    public void greaterThanOrEqualTo() {
        assertTrue(ServerVersion.from(0, 0, 0).greaterThanOrEqualTo(ServerVersion.from(0, 0, 0)));
        assertTrue(ServerVersion.from(1, 0, 0).greaterThanOrEqualTo(ServerVersion.from(0, 0, 0)));
        assertTrue(ServerVersion.from(1, 0, 0).greaterThanOrEqualTo(ServerVersion.from(1, 0, 0)));
        assertTrue(ServerVersion.from(2, 0, 0).greaterThanOrEqualTo(ServerVersion.from(1, 0, 0)));
        assertTrue(ServerVersion.from(2, 0, 0).greaterThanOrEqualTo(ServerVersion.from(2, 0, 0)));
        assertTrue(ServerVersion.from(2, 1, 0).greaterThanOrEqualTo(ServerVersion.from(2, 0, 0)));

        assertFalse(ServerVersion.from(0, 0, 0).greaterThanOrEqualTo(ServerVersion.from(1, 0, 0)));
        assertFalse(ServerVersion.from(1, 0, 0).greaterThanOrEqualTo(ServerVersion.from(2, 0, 0)));
        assertFalse(ServerVersion.from(2, 0, 0).greaterThanOrEqualTo(ServerVersion.from(2, 1, 0)));
    }

    @Test
    public void inRange() {
        ServerVersion v100 = ServerVersion.from(1, 0, 0);
        ServerVersion v120 = ServerVersion.from(1, 2, 0);
        ServerVersion v130 = ServerVersion.from(1, 3, 0);
        ServerVersion v200 = ServerVersion.from(2, 0, 0);

        assertTrue(v120.inRange(v100, v200));
        assertTrue(v120.inRange(v120, v200));
        assertTrue(v120.inRange(v100, v120));
        assertFalse(v120.inRange(v130, v200));

        assertTrue(v120.inRange(v100.upTo(v200)));
        assertTrue(v120.inRange(v120.upTo(v200)));
        assertFalse(v120.inRange(v100.upTo(v120)));
        assertFalse(v120.inRange(v130.upTo(v200)));

        assertTrue(v120.inRange(v100.upToAndIncluding(v200)));
        assertTrue(v120.inRange(v120.upToAndIncluding(v200)));
        assertTrue(v120.inRange(v100.upToAndIncluding(v120)));
        assertFalse(v120.inRange(v130.upToAndIncluding(v200)));
    }

    @Test
    public void assertAtLeast() {
        ServerVersion.from(0, 0, 0).assertAtLeast(ServerVersion.from(0, 0, 0));
        ServerVersion.from(1, 0, 0).assertAtLeast(ServerVersion.from(1, 0, 0));
        ServerVersion.from(1, 1, 0).assertAtLeast(ServerVersion.from(1, 0, 0));
        ServerVersion.from(1, 1, 0).assertAtLeast(ServerVersion.from(1, 1, 0));
        ServerVersion.from(2, 0, 0).assertAtLeast(ServerVersion.from(1, 0, 0));
        ServerVersion.from(2, 0, 0).assertAtLeast(ServerVersion.from(1, 1, 0));
        ServerVersion.from(2, 0, 0).assertAtLeast(ServerVersion.from(2, 0, 0));
        ServerVersion.from(2, 1, 0).assertAtLeast(ServerVersion.from(2, 1, 0));

        try {
            ServerVersion.from(0, 0, 0).assertAtLeast(ServerVersion.from(1, 0, 0));
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }

        try {
            ServerVersion.from(1, 0, 0).assertAtLeast(ServerVersion.from(1, 1, 0));
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }

        try {
            ServerVersion.from(1, 0, 0).assertAtLeast(ServerVersion.from(2, 0, 0));
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }

        try {
            ServerVersion.from(2, 0, 0).assertAtLeast(ServerVersion.from(2, 1, 0));
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }
    }
}
