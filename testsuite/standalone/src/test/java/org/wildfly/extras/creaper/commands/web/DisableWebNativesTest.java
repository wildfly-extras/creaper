package org.wildfly.extras.creaper.commands.web;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class DisableWebNativesTest {
    private static final Logger log = Logger.getLogger(DisableWebNativesTest.class);

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        // ignore whitespaces difference in "text" node
        XMLUnit.setNormalizeWhitespace(true);
    }

    private static final String SUBSYSTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\" %s />\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\" native=\"false\" />\n"
            + "    </profile>\n"
            + "</server>";

    @Test
    public void redefineNatives() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYSTEM_ORIGINAL, "native=\"true\"");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new DisableWebNatives());
        assertXmlIdentical(SUBSYSTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void disableNatives() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYSTEM_ORIGINAL, "");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new DisableWebNatives());
        assertXmlIdentical(SUBSYSTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
