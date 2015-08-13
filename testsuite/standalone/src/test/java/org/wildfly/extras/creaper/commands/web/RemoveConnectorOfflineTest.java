package org.wildfly.extras.creaper.commands.web;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;
import static org.junit.Assert.fail;

/**
 * Testing {@link org.wildfly.extras.creaper.commands.web.RemoveConnector} offline command.
 */
public class RemoveConnectorOfflineTest {
    private static final Logger log = Logger.getLogger(RemoveConnectorOfflineTest.class);

    private static final String TEST_CONNECTOR_NAME = "test-offline-http";
    private static final String SECOND_CONNECTOR_NAME = "second-connector";

    private static final String SUBSYSTEM_NO_CONNECTORS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_TWO_CONNECTORS = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "            <connector name=\"" + TEST_CONNECTOR_NAME + "\" />\n"
            + "            <connector name=\"" + SECOND_CONNECTOR_NAME + "\" />\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_SECOND_CONNECTOR = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "            <connector name=\"" + SECOND_CONNECTOR_NAME + "\" />\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void removeConnector_moreConnectorsExists() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_TWO_CONNECTORS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_TWO_CONNECTORS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new RemoveConnector(TEST_CONNECTOR_NAME));
        assertXmlIdentical(SUBSYSTEM_SECOND_CONNECTOR, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeConnector_connectorNotExist() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(SUBSYSTEM_NO_CONNECTORS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        assertXmlIdentical(SUBSYSTEM_NO_CONNECTORS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(new RemoveConnector(TEST_CONNECTOR_NAME));
        fail("Connector " + TEST_CONNECTOR_NAME + " Doesn't exist, command should fail");
    }
}
