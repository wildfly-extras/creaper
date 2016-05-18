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

public class AddAuditLogTcpSyslogHandlerOfflineTest {

    private static final String TEST_HANDLER_NAME = "creaper-tcp-handler";

    private static final String NO_HANDLERS = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers/>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String ADDED_TCP_HANDLER = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers>\n"
            + "                 <syslog-handler name=\"creaper-tcp-handler\" app-name=\"appName\" formatter=\"json-formatter\" max-failure-count=\"20\" syslog-format=\"RFC5424\" max-length=\"2048\" truncate=\"true\" facility=\"NETWORK_NEWS\">\n"
            + "                    <tcp host=\"127.0.0.1\" port=\"514\" message-transfer=\"OCTET_COUNTING\" reconnect-timeout=\"-1\"/>\n"
            + "                </syslog-handler>"
            + "           </handlers>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String REPLACED_TCP_HANDLER_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers>\n"
            + "                 <syslog-handler name=\"creaper-tcp-handler\" app-name=\"application-name\" formatter=\"json-formatter\" max-failure-count=\"15\" syslog-format=\"RFC3164\" max-length=\"1024\" truncate=\"false\" facility=\"KERNEL\">\n"
            + "                    <tcp host=\"localhost\" port=\"9898\" message-transfer=\"NON_TRANSPARENT_FRAMING\" reconnect-timeout=\"30\"/>\n"
            + "                </syslog-handler>"
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
    public void addTcpHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .appName("appName")
                .facility(SyslogFacilityType.NETWORK_NEWS)
                .formatter("json-formatter")
                .maxFailureCount(20)
                .maxLength(2048)
                .syslogFormat(SyslogFormatType.RFC5424)
                .truncate(true)
                .port(514)
                .host("127.0.0.1")
                .messageTransfer(MessageTransferType.OCTET_COUNTING)
                .reconnectTimeout(-1)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTcpSyslogHandler);

        assertXmlIdentical(ADDED_TCP_HANDLER, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void overrideExistingHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ADDED_TCP_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .appName("application-name")
                .facility(SyslogFacilityType.KERNEL)
                .formatter("json-formatter")
                .maxFailureCount(15)
                .maxLength(1024)
                .syslogFormat(SyslogFormatType.RFC3164)
                .truncate(false)
                .port(9898)
                .host("localhost")
                .messageTransfer(MessageTransferType.NON_TRANSPARENT_FRAMING)
                .reconnectTimeout(30)
                .replaceExisting()
                .build();

        assertXmlIdentical(ADDED_TCP_HANDLER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTcpSyslogHandler);

        assertXmlIdentical(REPLACED_TCP_HANDLER_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void overrideExistingHandler_notAllowed() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(ADDED_TCP_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .appName("new-application-name")
                .formatter("json-formatter")
                .build();

        assertXmlIdentical(ADDED_TCP_HANDLER, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTcpSyslogHandler);

        fail("TCP handler creaper-tcp-handler already exists, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_nullName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(null)
                .formatter("json-formatter")
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTcpSyslogHandler);

        fail("Creating TCP handler with null name should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_emptyName() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder("")
                .formatter("json-formatter")
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTcpSyslogHandler);

        fail("Creating TCP handler with empty name should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_nullFormatter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .formatter(null)
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTcpSyslogHandler);

        fail("Creating TCP handler with null formatter should throw an exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTcpHandler_emptyFormatter() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        AddAuditLogSyslogHandler addTcpSyslogHandler = new AddAuditLogSyslogHandler.TcpBuilder(TEST_HANDLER_NAME)
                .formatter("")
                .build();

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
        client.apply(addTcpSyslogHandler);

        fail("Creating TCP handler with empty formatter should throw an exception");
    }
}
