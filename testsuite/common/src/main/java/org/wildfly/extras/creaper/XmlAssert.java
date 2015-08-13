package org.wildfly.extras.creaper;

import org.custommonkey.xmlunit.Diff;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * This class is a preferred alternative to {@link org.custommonkey.xmlunit.XMLAssert}. It has fewer methods (tailored
 * to the needs of this project) and uses better terms ({@code assertXmlSimilar} instead of {@code assertXMLEqual}).
 */
public final class XmlAssert {
    private XmlAssert() {} // avoid instantiation

    /** @see Diff#identical() */
    public static void assertXmlIdentical(String expected, String actual) throws IOException, SAXException {
        Diff diff = new Diff(expected, actual);
        if (!diff.identical()) {
            fail(diff.toString());
        }
    }

    /** @see Diff#similar() */
    public static void assertXmlSimilar(String expected, String actual) throws IOException, SAXException {
        Diff diff = new Diff(expected, actual);
        if (!diff.similar()) {
            fail(diff.toString());
        }
    }
}
