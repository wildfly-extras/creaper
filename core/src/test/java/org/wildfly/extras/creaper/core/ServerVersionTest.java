package org.wildfly.extras.creaper.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerVersionTest {
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
