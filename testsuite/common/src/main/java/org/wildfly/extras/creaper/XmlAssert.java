package org.wildfly.extras.creaper;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

import static org.junit.Assert.assertFalse;

/**
 * This class is a preferred alternative to XMLUnit 1.x {@link org.custommonkey.xmlunit.XMLAssert}. It has fewer
 * methods (tailored to the needs of this project) and uses better terms ({@code assertXmlSimilar} instead of
 * {@code assertXMLEqual}).
 */
public final class XmlAssert {
    private static boolean ignoreWhitespace = false;
    private static boolean normalizeWhitespace = false;

    private XmlAssert() {
        // avoid instantiation
    }

    /** @see DiffBuilder#checkForIdentical() */
    public static void assertXmlIdentical(String expected, String actual) {
        Diff diff = createDiffBuilder(expected, actual)
                .checkForIdentical()
                .build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    /** @see DiffBuilder#checkForSimilar() */
    public static void assertXmlSimilar(String expected, String actual) {
        Diff diff = createDiffBuilder(expected, actual)
                .checkForSimilar()
                .build();
        assertFalse(diff.toString(), diff.hasDifferences());
    }

    /**
     * Whether to ignore whitespace when comparing node values.
     *
     * <p>Setting this parameter has no effect on {@link #setNormalizeWhitespace whitespace inside texts}.</p>
     */
    public static void setIgnoreWhitespace(boolean ignore) {
        ignoreWhitespace = ignore;
    }

    /**
     * Whether whitespace characters inside text nodes or attributes should be "normalized".
     *
     * <p>Normalized in this context means that all whitespace is replaced by the space character and adjacent
     * whitespace characters are collapsed to a single space character. It will also trim the resulting character
     * content on both ends.</p>
     *
     * <p>The default value is false.</p>
     *
     * <p>Setting this parameter has no effect on {@link #setIgnoreWhitespace ignorable whitespace}.</p>
     */
    public static void setNormalizeWhitespace(boolean normalize) {
        normalizeWhitespace = normalize;
    }

    private static DiffBuilder createDiffBuilder(String expected, String actual) {
        DiffBuilder diffBuilder =  DiffBuilder.compare(expected).withTest(actual)
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName));
        if (ignoreWhitespace)
            diffBuilder.ignoreWhitespace();
        if (normalizeWhitespace)
            diffBuilder.normalizeWhitespace();
        return diffBuilder;
    }
}
