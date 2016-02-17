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
