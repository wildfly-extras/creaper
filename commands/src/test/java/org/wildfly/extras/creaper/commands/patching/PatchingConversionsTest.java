package org.wildfly.extras.creaper.commands.patching;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class PatchingConversionsTest {
    @Test
    public void flattenEmptyListTest() {
        assertEquals("Empty collection should produce empty String", "",
                PatchingConversions.flatten(Collections.<String>emptyList()));
    }

    @Test
    public void flattenAndEscapeEmptyListTest() {
        assertEquals("Empty collection should produce empty String", "",
                PatchingConversions.flattenAndEscape(Collections.<String>emptyList()));
    }

    @Test
    public void flattenSingleElementListTest() {
        String[] singleElementList = new String[]{"sometext"};
        assertEquals("Empty collection should produce empty String", "sometext",
                PatchingConversions.flatten(Arrays.asList(singleElementList)));
    }

    @Test
    public void flattenSingleElementListWithBackslashesTest() {
        String[] singleElementList = new String[]{"some\\text"};
        assertEquals("Empty collection should produce empty String", "some\\text",
                PatchingConversions.flatten(Arrays.asList(singleElementList)));
    }

    @Test
    public void flattenAndEscapeSingleElementListWithBackslashesTest() {
        String[] singleElementList = new String[]{"some\\text"};
        assertEquals("Empty collection should produce empty String", "some\\\\text",
                PatchingConversions.flattenAndEscape(Arrays.asList(singleElementList)));
    }

    @Test
    public void flattenMultipleElementListWithBackslashesTest() {
        String[] singleElementList = new String[]{"some\\text", "another text"};
        assertEquals("Empty collection should produce empty String", "some\\text,another text",
                PatchingConversions.flatten(Arrays.asList(singleElementList)));
    }

    @Test
    public void flattenAndEscapeMultipleElementListWithBackslashesTest() {
        String[] singleElementList = new String[]{"some\\text", "another text"};
        assertEquals("Empty collection should produce empty String", "some\\\\text,another text",
                PatchingConversions.flattenAndEscape(Arrays.asList(singleElementList)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void flattenListAsNull() {
        PatchingConversions.flatten(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void flattenAndEscapeListAsNull() {
        PatchingConversions.flatten(null);
    }
}
