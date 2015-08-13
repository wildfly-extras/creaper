package org.wildfly.extras.creaper.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ManagementVersionTest {
    @Test
    public void from() {
        assertEquals(ManagementVersion.VERSION_0_0_0, ManagementVersion.from(0, 0, 0));
        assertEquals(ManagementVersion.VERSION_1_0_0, ManagementVersion.from(1, 0, 0));
        assertEquals(ManagementVersion.VERSION_2_0_0, ManagementVersion.from(2, 0, 0));
        assertEquals(ManagementVersion.VERSION_2_1_0, ManagementVersion.from(2, 1, 0));

        assertNotEquals(ManagementVersion.VERSION_1_0_0, ManagementVersion.from(0, 0, 0));
        assertNotEquals(ManagementVersion.VERSION_2_0_0, ManagementVersion.from(1, 0, 0));
        assertNotEquals(ManagementVersion.VERSION_2_1_0, ManagementVersion.from(2, 0, 0));
        assertNotEquals(ManagementVersion.VERSION_0_0_0, ManagementVersion.from(2, 1, 0));
    }

    @Test
    public void equalTo() {
        assertTrue(ManagementVersion.VERSION_0_0_0.equalTo(ManagementVersion.VERSION_0_0_0));
        assertTrue(ManagementVersion.VERSION_1_0_0.equalTo(ManagementVersion.VERSION_1_0_0));
        assertTrue(ManagementVersion.VERSION_2_0_0.equalTo(ManagementVersion.VERSION_2_0_0));

        assertFalse(ManagementVersion.VERSION_0_0_0.equalTo(ManagementVersion.VERSION_1_0_0));
        assertFalse(ManagementVersion.VERSION_1_0_0.equalTo(ManagementVersion.VERSION_2_0_0));
        assertFalse(ManagementVersion.VERSION_2_0_0.equalTo(ManagementVersion.VERSION_0_0_0));
    }

    @Test
    public void lessThan() {
        assertTrue(ManagementVersion.VERSION_0_0_0.lessThan(ManagementVersion.VERSION_1_0_0));
        assertTrue(ManagementVersion.VERSION_1_0_0.lessThan(ManagementVersion.VERSION_2_0_0));
        assertTrue(ManagementVersion.VERSION_2_0_0.lessThan(ManagementVersion.VERSION_2_1_0));

        assertFalse(ManagementVersion.VERSION_1_0_0.lessThan(ManagementVersion.VERSION_0_0_0));
        assertFalse(ManagementVersion.VERSION_2_0_0.lessThan(ManagementVersion.VERSION_1_0_0));
        assertFalse(ManagementVersion.VERSION_2_1_0.lessThan(ManagementVersion.VERSION_2_0_0));
    }

    @Test
    public void lessThanOrEqualTo() {
        assertTrue(ManagementVersion.VERSION_0_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_0_0_0));
        assertTrue(ManagementVersion.VERSION_0_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_1_0_0));
        assertTrue(ManagementVersion.VERSION_1_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_1_0_0));
        assertTrue(ManagementVersion.VERSION_1_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_2_0_0));
        assertTrue(ManagementVersion.VERSION_2_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_2_0_0));
        assertTrue(ManagementVersion.VERSION_2_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_2_1_0));

        assertFalse(ManagementVersion.VERSION_1_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_0_0_0));
        assertFalse(ManagementVersion.VERSION_2_0_0.lessThanOrEqualTo(ManagementVersion.VERSION_1_0_0));
        assertFalse(ManagementVersion.VERSION_2_1_0.lessThanOrEqualTo(ManagementVersion.VERSION_2_0_0));
    }

    @Test
    public void greaterThan() {
        assertTrue(ManagementVersion.VERSION_1_0_0.greaterThan(ManagementVersion.VERSION_0_0_0));
        assertTrue(ManagementVersion.VERSION_2_0_0.greaterThan(ManagementVersion.VERSION_1_0_0));
        assertTrue(ManagementVersion.VERSION_2_1_0.greaterThan(ManagementVersion.VERSION_2_0_0));

        assertFalse(ManagementVersion.VERSION_0_0_0.greaterThan(ManagementVersion.VERSION_1_0_0));
        assertFalse(ManagementVersion.VERSION_1_0_0.greaterThan(ManagementVersion.VERSION_2_0_0));
        assertFalse(ManagementVersion.VERSION_2_0_0.greaterThan(ManagementVersion.VERSION_2_1_0));
    }

    @Test
    public void greaterThanOrEqualTo() {
        assertTrue(ManagementVersion.VERSION_0_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_0_0_0));
        assertTrue(ManagementVersion.VERSION_1_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_0_0_0));
        assertTrue(ManagementVersion.VERSION_1_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_1_0_0));
        assertTrue(ManagementVersion.VERSION_2_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_1_0_0));
        assertTrue(ManagementVersion.VERSION_2_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_2_0_0));
        assertTrue(ManagementVersion.VERSION_2_1_0.greaterThanOrEqualTo(ManagementVersion.VERSION_2_0_0));

        assertFalse(ManagementVersion.VERSION_0_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_1_0_0));
        assertFalse(ManagementVersion.VERSION_1_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_2_0_0));
        assertFalse(ManagementVersion.VERSION_2_0_0.greaterThanOrEqualTo(ManagementVersion.VERSION_2_1_0));
    }

    @Test
    public void assertAtLeast() {
        ManagementVersion.VERSION_0_0_0.assertAtLeast(ManagementVersion.VERSION_0_0_0);
        ManagementVersion.VERSION_1_0_0.assertAtLeast(ManagementVersion.VERSION_1_0_0);
        ManagementVersion.VERSION_1_1_0.assertAtLeast(ManagementVersion.VERSION_1_0_0);
        ManagementVersion.VERSION_1_1_0.assertAtLeast(ManagementVersion.VERSION_1_1_0);
        ManagementVersion.VERSION_2_0_0.assertAtLeast(ManagementVersion.VERSION_1_0_0);
        ManagementVersion.VERSION_2_0_0.assertAtLeast(ManagementVersion.VERSION_1_1_0);
        ManagementVersion.VERSION_2_0_0.assertAtLeast(ManagementVersion.VERSION_2_0_0);
        ManagementVersion.VERSION_2_1_0.assertAtLeast(ManagementVersion.VERSION_2_1_0);

        try {
            ManagementVersion.VERSION_0_0_0.assertAtLeast(ManagementVersion.VERSION_1_0_0);
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }

        try {
            ManagementVersion.VERSION_1_0_0.assertAtLeast(ManagementVersion.VERSION_1_1_0);
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }

        try {
            ManagementVersion.VERSION_1_0_0.assertAtLeast(ManagementVersion.VERSION_2_0_0);
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }

        try {
            ManagementVersion.VERSION_2_0_0.assertAtLeast(ManagementVersion.VERSION_2_1_0);
            throw new RuntimeException("failed"); // fail() would be caught immediately
        } catch (AssertionError ignored) {
        }
    }
}
