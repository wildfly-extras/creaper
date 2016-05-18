package org.wildfly.extras.creaper.commands.auditlog;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.junit.Assert.fail;
import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class AddAuditLogTlsSyslogHandlerOfflineTest {

    private static final String TEST_HANDLER_NAME = "creaper-tls-handler";

    private static final String NO_HANDLERS = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers/>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String ADDED_TLS_TRUSTSTORE_HANDLER = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers>\n"
            + "                <syslog-handler name=\"creaper-tls-handler\" app-name=\"appName\" formatter=\"json-formatter\" max-failure-count=\"20\" syslog-format=\"RFC5424\" max-length=\"2048\" truncate=\"true\" facility=\"NETWORK_NEWS\">\n"
            + "                    <tls host=\"127.0.0.1\" port=\"514\" message-transfer=\"OCTET_COUNTING\" reconnect-timeout=\"-1\">\n"
            + "                        <truststore keystore-password=\"keystorePass\" keystore-path=\"trust.keystore\" keystore-relative-to=\"jboss.dir/truststore\"/>"
            + "                    </tls>"
            + "                </syslog-handler>\n"
            + "           </handlers>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String ADDED_TLS_CLIENT_CERT_HANDLER = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers>\n"
            + "                <syslog-handler name=\"creaper-tls-handler\" app-name=\"application-name\" formatter=\"json-formatter\" max-failure-count=\"15\" syslog-format=\"RFC3164\" max-length=\"1024\" truncate=\"true\" facility=\"KERNEL\">\n"
            + "                    <tls host=\"localhost\" port=\"9898\" message-transfer=\"NON_TRANSPARENT_FRAMING\" reconnect-timeout=\"30\">\n"
            + "                        <client-certificate-store keystore-password=\"keystorePassword\" key-password=\"keyPassword\" keystore-path=\"clientCert.keystore\" keystore-relative-to=\"jboss.dir/clientCert\"/>"
            + "                    </tls>"
            + "                </syslog-handler>\n"
            + "           </handlers>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void addTlsTruststoreSyslogHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .appName("appName")
                .facility(SyslogFacilityType.NETWORK_NEWS)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(true)
                .messageTransfer(MessageTransferType.OCTET_COUNTING)
                .reconnectTimeout(-1)
                .port(514)
                .host("127.0.0.1")
                .keystorePassword("keystorePass")
                .keystorePath("trust.keystore")
                .keystoreRelativeTo("jboss.dir/truststore")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        assertXmlIdentical(ADDED_TLS_TRUSTSTORE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void addTlsClientCertSyslogHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.KERNEL)
                .formatter("json-formatter")
                .maxFailureCount(15)
                .maxLength(1024)
                .syslogFormat(SyslogFormatType.RFC3164)
                .truncate(true)
                .messageTransfer(MessageTransferType.NON_TRANSPARENT_FRAMING)
                .reconnectTimeout(30)
                .port(9898)
                .host("localhost")
                .keystorePassword("keystorePassword")
                .keyPassword("keyPassword")
                .keystorePath("clientCert.keystore")
                .keystoreRelativeTo("jboss.dir/clientCert")
                .authenticationType(AuthenticationType.CLIENT_CERTIFICATE_STORE)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        assertXmlIdentical(ADDED_TLS_CLIENT_CERT_HANDLER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideExistingHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ADDED_TLS_CLIENT_CERT_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .appName("appName")
                .facility(SyslogFacilityType.NETWORK_NEWS)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(true)
                .messageTransfer(MessageTransferType.OCTET_COUNTING)
                .reconnectTimeout(-1)
                .port(514)
                .host("127.0.0.1")
                .keystorePassword("keystorePass")
                .keystorePath("trust.keystore")
                .keystoreRelativeTo("jboss.dir/truststore")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .replaceExisting()
                .build();

        assertXmlIdentical(ADDED_TLS_CLIENT_CERT_HANDLER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        assertXmlIdentical(ADDED_TLS_TRUSTSTORE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingHandler_notAllowed() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ADDED_TLS_TRUSTSTORE_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .appName("new-application-name")
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();

        assertXmlIdentical(ADDED_TLS_TRUSTSTORE_HANDLER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("TLS handler creaper-tls-handler already exists, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(null)
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("Creating TLS handler with null name should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_emptyName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder("")
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();
        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("Creating TLS handler with empty name should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullFormatter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .formatter(null)
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("Creating TLS handler with null formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_emptyFormatter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .formatter("")
                .keystorePassword("keystorePassword")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("Creating TLS handler with empty formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullKeystorePassword() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword(null)
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("Creating TLS handler with empty formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_emptyKeystorePassword() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("")
                .authenticationType(AuthenticationType.TRUSTSTORE)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("Creating TLS handler with empty formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTlsHandler_nullAuthenticationType() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTlsSyslogHandler = new AddAuditLogSyslogHandler.TlsBuilder(TEST_HANDLER_NAME)
                .formatter("json-formatter")
                .keystorePassword("keystorePassword")
                .authenticationType(null)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTlsSyslogHandler);

        fail("Creating TLS handler with empty formatter should throw an exception");
    }
}
