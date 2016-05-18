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

public class RemoveAuditLogSyslogHandlerOfflineTest {

    private static final String TEST_UDP_SYSLOG_HANDLER = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers>\n"
            + "                 <syslog-handler name=\"creaper-udp-handler\" app-name=\"appName\" formatter=\"json-formatter\" max-failure-count=\"20\" syslog-format=\"RFC5424\" max-length=\"2048\" truncate=\"true\" facility=\"NETWORK_NEWS\">\n"
            + "                    <udp host=\"127.0.0.1\" port=\"514\"/>\n"
            + "                </syslog-handler>"
            + "           </handlers>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String TEST_TCP_SYSLOG_HANDLER = ""
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

    private static final String TEST_TLS_SYSLOG_HANDLER = ""
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

    private static final String NO_HANDLERS = ""
            + "<server xmlns=\"urn:jboss:domain:4.1\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <handlers/>\n"
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
    public void removeUdpHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TEST_UDP_SYSLOG_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuditLogSyslogHandler removeHandler = new RemoveAuditLogSyslogHandler("creaper-udp-handler");

        assertXmlIdentical(TEST_UDP_SYSLOG_HANDLER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(removeHandler);
        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeTcpHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TEST_TCP_SYSLOG_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuditLogSyslogHandler removeHandler = new RemoveAuditLogSyslogHandler("creaper-tcp-handler");

        assertXmlIdentical(TEST_TCP_SYSLOG_HANDLER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(removeHandler);
        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test
    public void removeTlsHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TEST_TLS_SYSLOG_HANDLER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuditLogSyslogHandler removeHandler = new RemoveAuditLogSyslogHandler("creaper-tls-handler");

        assertXmlIdentical(TEST_TLS_SYSLOG_HANDLER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(removeHandler);
        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingSyslogHandler() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(NO_HANDLERS, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuditLogSyslogHandler removeHandler = new RemoveAuditLogSyslogHandler("non-existing-syslog-handler");

        assertXmlIdentical(NO_HANDLERS, Files.toString(cfg, Charsets.UTF_8));

        client.apply(removeHandler);
        fail("Specified syslog handler does not exist in configuration, an exception should be thrown");
    }
}
