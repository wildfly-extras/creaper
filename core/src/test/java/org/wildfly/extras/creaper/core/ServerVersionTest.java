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
        assertSame(ServerVersion.VERSION_0_0_0, ServerVersion.from(0, 0, 0));
        assertSame(ServerVersion.VERSION_1_0_0, ServerVersion.from(1, 0, 0));
        assertSame(ServerVersion.VERSION_1_1_0, ServerVersion.from(1, 1, 0));
        assertSame(ServerVersion.VERSION_1_2_0, ServerVersion.from(1, 2, 0));
        assertSame(ServerVersion.VERSION_1_3_0, ServerVersion.from(1, 3, 0));
        assertSame(ServerVersion.VERSION_1_4_0, ServerVersion.from(1, 4, 0));
        assertSame(ServerVersion.VERSION_1_5_0, ServerVersion.from(1, 5, 0));
        assertSame(ServerVersion.VERSION_1_6_0, ServerVersion.from(1, 6, 0));
        assertSame(ServerVersion.VERSION_1_7_0, ServerVersion.from(1, 7, 0));
        assertSame(ServerVersion.VERSION_1_8_0, ServerVersion.from(1, 8, 0));
        assertSame(ServerVersion.VERSION_2_0_0, ServerVersion.from(2, 0, 0));
        assertSame(ServerVersion.VERSION_2_1_0, ServerVersion.from(2, 1, 0));
        assertSame(ServerVersion.VERSION_2_2_0, ServerVersion.from(2, 2, 0));
        assertSame(ServerVersion.VERSION_3_0_0, ServerVersion.from(3, 0, 0));
        assertSame(ServerVersion.VERSION_4_0_0, ServerVersion.from(4, 0, 0));
        assertSame(ServerVersion.VERSION_4_1_0, ServerVersion.from(4, 1, 0));
        assertSame(ServerVersion.VERSION_4_2_0, ServerVersion.from(4, 2, 0));
        assertSame(ServerVersion.VERSION_5_0_0, ServerVersion.from(5, 0, 0));
        assertSame(ServerVersion.VERSION_6_0_0, ServerVersion.from(6, 0, 0));
        assertSame(ServerVersion.VERSION_7_0_0, ServerVersion.from(7, 0, 0));
        assertSame(ServerVersion.VERSION_8_0_0, ServerVersion.from(8, 0, 0));
        assertSame(ServerVersion.VERSION_9_0_0, ServerVersion.from(9, 0, 0));
        assertSame(ServerVersion.VERSION_10_0_0, ServerVersion.from(10, 0, 0));
        assertSame(ServerVersion.VERSION_12_0_0, ServerVersion.from(12, 0, 0));
        assertSame(ServerVersion.VERSION_13_0_0, ServerVersion.from(13, 0, 0));
        assertSame(ServerVersion.VERSION_14_0_0, ServerVersion.from(14, 0, 0));
        assertSame(ServerVersion.VERSION_15_0_0, ServerVersion.from(15, 0, 0));
        assertSame(ServerVersion.VERSION_16_0_0, ServerVersion.from(16, 0, 0));
        assertSame(ServerVersion.VERSION_17_0_0, ServerVersion.from(17, 0, 0));
        assertSame(ServerVersion.VERSION_18_0_0, ServerVersion.from(18, 0, 0));
        assertSame(ServerVersion.VERSION_19_0_0, ServerVersion.from(19, 0, 0));
        assertSame(ServerVersion.VERSION_20_0_0, ServerVersion.from(20, 0, 0));

        assertNotSame(ServerVersion.from(42, 42, 42), ServerVersion.from(42, 42, 42));
    }

    @Test
    public void equality() {
        assertEquals(ServerVersion.VERSION_0_0_0, ServerVersion.from(0, 0, 0));
        assertEquals(ServerVersion.VERSION_1_0_0, ServerVersion.from(1, 0, 0));
        assertEquals(ServerVersion.VERSION_1_1_0, ServerVersion.from(1, 1, 0));
        assertEquals(ServerVersion.VERSION_1_2_0, ServerVersion.from(1, 2, 0));
        assertEquals(ServerVersion.VERSION_1_3_0, ServerVersion.from(1, 3, 0));
        assertEquals(ServerVersion.VERSION_1_4_0, ServerVersion.from(1, 4, 0));
        assertEquals(ServerVersion.VERSION_1_5_0, ServerVersion.from(1, 5, 0));
        assertEquals(ServerVersion.VERSION_1_6_0, ServerVersion.from(1, 6, 0));
        assertEquals(ServerVersion.VERSION_1_7_0, ServerVersion.from(1, 7, 0));
        assertEquals(ServerVersion.VERSION_1_8_0, ServerVersion.from(1, 8, 0));
        assertEquals(ServerVersion.VERSION_2_0_0, ServerVersion.from(2, 0, 0));
        assertEquals(ServerVersion.VERSION_2_1_0, ServerVersion.from(2, 1, 0));
        assertEquals(ServerVersion.VERSION_2_2_0, ServerVersion.from(2, 2, 0));
        assertEquals(ServerVersion.VERSION_3_0_0, ServerVersion.from(3, 0, 0));
        assertEquals(ServerVersion.VERSION_4_0_0, ServerVersion.from(4, 0, 0));
        assertEquals(ServerVersion.VERSION_4_1_0, ServerVersion.from(4, 1, 0));
        assertEquals(ServerVersion.VERSION_4_2_0, ServerVersion.from(4, 2, 0));
        assertEquals(ServerVersion.VERSION_5_0_0, ServerVersion.from(5, 0, 0));
        assertEquals(ServerVersion.VERSION_6_0_0, ServerVersion.from(6, 0, 0));
        assertEquals(ServerVersion.VERSION_7_0_0, ServerVersion.from(7, 0, 0));
        assertEquals(ServerVersion.VERSION_8_0_0, ServerVersion.from(8, 0, 0));
        assertEquals(ServerVersion.VERSION_9_0_0, ServerVersion.from(9, 0, 0));
        assertEquals(ServerVersion.VERSION_10_0_0, ServerVersion.from(10, 0, 0));
        assertEquals(ServerVersion.VERSION_12_0_0, ServerVersion.from(12, 0, 0));
        assertEquals(ServerVersion.VERSION_13_0_0, ServerVersion.from(13, 0, 0));
        assertSame(ServerVersion.VERSION_14_0_0, ServerVersion.from(14, 0, 0));
        assertSame(ServerVersion.VERSION_15_0_0, ServerVersion.from(15, 0, 0));
        assertSame(ServerVersion.VERSION_16_0_0, ServerVersion.from(16, 0, 0));
        assertSame(ServerVersion.VERSION_17_0_0, ServerVersion.from(17, 0, 0));
        assertSame(ServerVersion.VERSION_18_0_0, ServerVersion.from(18, 0, 0));
        assertSame(ServerVersion.VERSION_19_0_0, ServerVersion.from(19, 0, 0));
        assertSame(ServerVersion.VERSION_20_0_0, ServerVersion.from(20, 0, 0));

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
