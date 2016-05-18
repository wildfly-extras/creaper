package org.wildfly.extras.creaper.commands.auditlog;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

import static org.wildfly.extras.creaper.XmlAssert.assertXmlIdentical;

public class RemoveAuditLoggerOfflineTest {

    private static final String TEST_LOGGER = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log>\n"
            + "            <logger log-boot=\"true\" log-read-only=\"false\" enabled=\"false\"/>\n"
            + "        </audit-log>\n"
            + "    </management>\n"
            + "</server>";

    private static final String LOGGER_REMOVED_EXPECTED = ""
            + "<server xmlns=\"urn:jboss:domain:1.7\">\n"
            + "    <management>\n"
            + "        <audit-log/>\n"
            + "    </management>\n"
            + "</server>";

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    @Before
    public void setUp() {
        XMLUnit.setNormalizeWhitespace(true);
    }

    @Test
    public void removeLogger() throws Exception {
        File cfg = tmp.newFile("xmlTransform.xml");
        Files.write(TEST_LOGGER, cfg, Charsets.UTF_8);

        OfflineManagementClient client = ManagementClient.offline(
                OfflineOptions.standalone().configurationFile(cfg).build());

        RemoveAuditLogger removeLogger = new RemoveAuditLogger();

        assertXmlIdentical(TEST_LOGGER, Files.toString(cfg, Charsets.UTF_8));

        client.apply(removeLogger);
        assertXmlIdentical(LOGGER_REMOVED_EXPECTED, Files.toString(cfg, Charsets.UTF_8));
    }
}
