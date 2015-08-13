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

/**
 * Testing {@link AddConnector} offline command.
 */
public class AddConnectorSslConfigOfflineTest {
    private static final Logger log = Logger.getLogger(AddConnectorSslConfigOfflineTest.class);

    private static final String TEST_CONNECTOR_NAME = "test-offline-http";

    private static final String SUBSYSTEM_ORIGINAL = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "            <connector name=\"" + TEST_CONNECTOR_NAME + "\">\n"
            + "               %s\n"
            + "             </connector>\n"
            + "        </subsystem>\n"
            + "    </profile>\n"
            + "</server>";

    private static final String SUBSYSTEM_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <profile>\n"
            + "        <subsystem xmlns=\"urn:jboss:domain:web:2.2\">\n"
            + "            <connector name=\"" + TEST_CONNECTOR_NAME + "\">\n"
            + "               <ssl ca-certificate-file=\"/path/to/ca-certificate-file\" ca-certificate-password=\"password\" "
            + "                    ca-revocation-url=\"file:///revocationURL\" certificate-file=\"/path/to/certificate-file\""
            + "                    certificate-key-file=\"/path/to/certificate-key-file\" cipher-suite=\"cipherSuite\" key-alias=\"keyAlias\""
            + "                    keystore-type=\"JKS\" password=\"password\" protocol=\"protocol\" session-cache-size=\"100\""
            + "                    session-timeout=\"30\" ssl-protocol=\"TLS\" truststore-type=\"JKS\" verify-client=\"false\" verify-depth=\"1\""
            + "               />\n"
            + "             </connector>\n"
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
    public void replaceSsl() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYSTEM_ORIGINAL, "<ssl password=\"xxx\"/>");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddConnectorSslConfig addConnectorSslConfig = new AddConnectorSslConfig.Builder(TEST_CONNECTOR_NAME)
                .caCertificateFile("/path/to/ca-certificate-file")
                .caCertificatePassword("password")
                .caRevocationUrl("file:///revocationURL")
                .certificateFile("/path/to/certificate-file")
                .certificateKeyFile("/path/to/certificate-key-file")
                .cipherSuite("cipherSuite")
                .keyAlias("keyAlias")
                .keystoreType("JKS")
                .password("password")
                .protocol("protocol")
                .sessionCacheSize(100)
                .sessionTimeout(30)
                .sslProtocol("TLS")
                .truststoreType("JKS")
                .verifyClient("false")
                .verifyDepth(1)
                .build();
        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addConnectorSslConfig);
        assertXmlIdentical(SUBSYSTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void defineSsl() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        String subsystemXml = String.format(SUBSYSTEM_ORIGINAL, "");
        Files.write(subsystemXml, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddConnectorSslConfig addConnectorSslConfig = new AddConnectorSslConfig.Builder(TEST_CONNECTOR_NAME)
                .caCertificateFile("/path/to/ca-certificate-file")
                .caCertificatePassword("password")
                .caRevocationUrl("file:///revocationURL")
                .certificateFile("/path/to/certificate-file")
                .certificateKeyFile("/path/to/certificate-key-file")
                .cipherSuite("cipherSuite")
                .keyAlias("keyAlias")
                .keystoreType("JKS")
                .password("password")
                .protocol("protocol")
                .sessionCacheSize(100)
                .sessionTimeout(30)
                .sslProtocol("TLS")
                .truststoreType("JKS")
                .verifyClient("false")
                .verifyDepth(1)
                .build();
        assertXmlIdentical(subsystemXml, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addConnectorSslConfig);
        assertXmlIdentical(SUBSYSTEM_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
