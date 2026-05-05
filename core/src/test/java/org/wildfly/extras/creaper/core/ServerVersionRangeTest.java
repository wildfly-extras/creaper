package org.wildfly.extras.creaper.core;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerVersionRangeTest {
    private ServerVersionRange range(boolean lowIncluded, boolean highIncluded) {
        return new ServerVersionRange(ServerVersion.VERSION_20_0_0, lowIncluded,
                ServerVersion.VERSION_23_0_0, highIncluded);
    }

    @Test
    public void lowIncludedHighIncluded() {
        ServerVersionRange range = range(true, true);
        assertFalse(range.contains(ServerVersion.from(19, 0, 0)));
        assertTrue(range.contains(ServerVersion.VERSION_20_0_0));
        assertTrue(range.contains(ServerVersion.VERSION_21_0_0));
        assertTrue(range.contains(ServerVersion.VERSION_23_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_24_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_25_0_0));
    }

    @Test
    public void lowIncludedHighExcluded() {
        ServerVersionRange range = range(true, false);
        assertFalse(range.contains(ServerVersion.from(19, 0, 0)));
        assertTrue(range.contains(ServerVersion.VERSION_20_0_0));
        assertTrue(range.contains(ServerVersion.VERSION_21_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_23_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_24_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_25_0_0));
    }

    @Test
    public void lowExcludedHighIncluded() {
        ServerVersionRange range = range(false, true);
        assertFalse(range.contains(ServerVersion.from(19, 0, 0)));
        assertFalse(range.contains(ServerVersion.VERSION_20_0_0));
        assertTrue(range.contains(ServerVersion.VERSION_21_0_0));
        assertTrue(range.contains(ServerVersion.VERSION_23_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_24_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_25_0_0));
    }

    @Test
    public void lowExcludedHighExcluded() {
        ServerVersionRange range = range(false, false);
        assertFalse(range.contains(ServerVersion.from(19, 0, 0)));
        assertFalse(range.contains(ServerVersion.VERSION_20_0_0));
        assertTrue(range.contains(ServerVersion.VERSION_21_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_23_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_24_0_0));
        assertFalse(range.contains(ServerVersion.VERSION_25_0_0));
    }

    @Test
    public void emptyRange() {
        assertTrue(ServerVersion.VERSION_20_0_0.inRange(
                ServerVersion.VERSION_20_0_0.upTo(ServerVersion.VERSION_20_0_0)));
        assertTrue(ServerVersion.VERSION_20_0_0.inRange(
                ServerVersion.VERSION_20_0_0.upToAndIncluding(ServerVersion.VERSION_20_0_0)));

        assertFalse(ServerVersion.VERSION_23_0_0.inRange(
                ServerVersion.VERSION_20_0_0.upTo(ServerVersion.VERSION_20_0_0)));
        assertFalse(ServerVersion.VERSION_23_0_0.inRange(
                ServerVersion.VERSION_20_0_0.upToAndIncluding(ServerVersion.VERSION_20_0_0)));
    }
}
