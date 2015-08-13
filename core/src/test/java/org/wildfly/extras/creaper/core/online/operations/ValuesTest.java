package org.wildfly.extras.creaper.core.online.operations;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ValuesTest {
    @Test
    public void noValue() {
        assertEquals(0, Values.empty().size());
    }

    @Test
    public void oneValue() {
        assertEquals(1, Values.of("foo", "bar").size());
        assertEquals(1, Values.ofList("foo", "bar", "baz").size());
        assertEquals(1, Values.ofObject("foo", Values.empty()).size());
    }

    @Test
    public void twoValues() {
        assertEquals(2, Values.of("foo", "bar").and("baz", "quux").size());
        assertEquals(2, Values.of("foo", "bar").andList("baz", "qux", "quux").size());
        assertEquals(2, Values.of("foo", "bar").andObject("baz", Values.empty()).size());
        assertEquals(2, Values.ofList("foo", "bar", "baz").and("qux", "quux").size());
        assertEquals(2, Values.ofObject("foo", Values.empty()).and("bar", "baz").size());
        assertEquals(2, Values.ofList("foo", "bar", "baz").andObject("quux", Values.empty()).size());
        assertEquals(2, Values.ofObject("foo", Values.empty()).andList("bar", "baz", "quux").size());
    }

    @Test
    public void fromMap() {
        assertNull(Values.fromMap(null));
        assertNull(Values.fromMap(ImmutableMap.<String, String>of()));

        assertNotNull(Values.fromMap(ImmutableMap.of("foo", "bar")));
        assertEquals(1, Values.fromMap(ImmutableMap.of("foo", "bar")).size());

        assertNotNull(Values.fromMap(ImmutableMap.of("foo", "bar", "baz", "quux")));
        assertEquals(2, Values.fromMap(ImmutableMap.of("foo", "bar", "baz", "quux")).size());
    }
}
