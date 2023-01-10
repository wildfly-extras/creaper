package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.extras.creaper.XmlAssert;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class FirstLevelXmlElementOrderTest {
    private static final String DOMAIN_XML = ""
            + "<?xml version=\"1.0\" ?>\n"
            + "<domain xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <extensions/>\n"
            + "    <system-properties/>\n"
            + "    <management/>\n"
            + "    <profiles/>\n"
            + "    <interfaces/>\n"
            + "    <socket-binding-groups/>\n"
            + "    <server-groups/>\n"
            + "    <host-excludes/>\n"
            + "</domain>";

    private static final String HOST_XML = ""
            + "<?xml version=\"1.0\" ?>\n"
            + "<host xmlns=\"urn:jboss:domain:4.1\" name=\"master\">\n"
            + "    <extensions/>\n"
            + "    <management/>\n"
            + "    <domain-controller/>\n"
            + "    <interfaces/>\n"
            + "    <jvms/>\n"
            + "    <servers/>\n"
            + "    <profile/>\n"
            + "</host>";

    private static final String SERVER_XML = ""
            + "<?xml version=\"1.0\"?>\n"
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <extensions/>\n"
            + "    <management/>\n"
            + "    <profile/>\n"
            + "    <interfaces/>\n"
            + "    <socket-binding-group/>\n"
            + "</server>";

    private static final String UNKNOWN_XML = "<foobar/>";

    @BeforeClass
    public static void setUpXmlUnit() {
        XmlAssert.setIgnoreWhitespace(true);
    }

    @Test
    public void domain() throws IOException, SAXException {
        assertXmlIdentical(DOMAIN_XML, FirstLevelXmlElementOrder.fix(DOMAIN_XML));
    }

    @Test
    public void host() throws IOException, SAXException {
        assertXmlIdentical(HOST_XML, FirstLevelXmlElementOrder.fix(HOST_XML));
    }

    @Test
    public void server() throws IOException, SAXException {
        assertXmlIdentical(SERVER_XML, FirstLevelXmlElementOrder.fix(SERVER_XML));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknown() {
        FirstLevelXmlElementOrder.fix(UNKNOWN_XML);
    }
}
