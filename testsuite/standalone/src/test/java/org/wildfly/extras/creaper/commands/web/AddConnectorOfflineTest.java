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
 * Testing {@link org.wildfly.extras.creaper.commands.web.AddConnector} offline command.
 */
public class AddConnectorOfflineTest {
    private static final Logger log = Logger.getLogger(AddConnectorOfflineTest.class);

    private static final String TEST_CONNECTOR_NAME = "test-offline-http";

    private static final String SUBSYSTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "                 %s\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";
    private static final String SUBSYSTEM_EXPECTED_REPLACE = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "            <connector name=\"" + TEST_CONNECTOR_NAME + "\" protocol=\"HTTP/1.1\" scheme=\"http\" socket-binding=\"http\" "
            + "                       enabled=\"false\" />\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "            <connector name=\"" + TEST_CONNECTOR_NAME + "\" protocol=\"HTTP/1.1\" scheme=\"http\" socket-binding=\"http\" "
            + "                           enabled=\"false\" enable-lookups=\"false\" max-connections=\"10\" max-post-size=\"20\" "
            + "                           max-save-post-size=\"20\" proxy-binding=\"test-binding\" proxy-name=\"test-proxy\" executor=\"test-executor\" "
            + "                           proxy-port=\"7000\" redirect-binding=\"https\" redirect-port=\"8443\" secure=\"false\">"
            + "                <virtual-server name=\"default-host\"/>\n"
            + "            </connector>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test(expected = CommandFailedException.class)
    public void addConnector_forbiddenToReplaceExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYSTEM_ORIGINAL, "<connector name=\"" + TEST_CONNECTOR_NAME + "\"/>");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddConnector addConnector = new AddConnector.Builder(TEST_CONNECTOR_NAME)
                .protocol("HTTP/1.1")
                .scheme("http")
                .socketBinding("http")
                .enabled(false)
                .build();

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addConnector);
        fail("Connector " + TEST_CONNECTOR_NAME + " already exists in configuration. Exception is expected.");
    }

    @Test
    public void addConnector_overrideExisting() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYSTEM_ORIGINAL, "<connector name=\"" + TEST_CONNECTOR_NAME + "\"/>");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddConnector addConnector = new AddConnector.Builder(TEST_CONNECTOR_NAME)
                .protocol("HTTP/1.1")
                .scheme("http")
                .socketBinding("http")
                .enabled(false)
                .replaceExisting()
                .build();

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addConnector);
        assertXmlIdentical(SUBSYSTEM_EXPECTED_REPLACE, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addConnector_createNew() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYSTEM_ORIGINAL, "");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddConnector addConnector = new AddConnector.Builder(TEST_CONNECTOR_NAME)
                .protocol("HTTP/1.1")
                .scheme("http")
                .socketBinding("http")
                .enabled(false)
                .enableLookups(false)
                .maxConnections(10)
                .maxPostSize(20)
                .maxSavePostSize(20)
                .proxyBinding("test-binding")
                .proxyName("test-proxy")
                .proxyPort(7000)
                .redirectBinding("https")
                .redirectPort(8443)
                .secure(false)
                .virtualServer("default-host")
                .executor("test-executor")
                .build();

        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addConnector);
        assertXmlIdentical(SUBSYSTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
